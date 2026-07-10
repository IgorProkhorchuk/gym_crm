package com.epam.gymcrm.workload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.model.TrainerWorkload;
import com.epam.gymcrm.workload.model.TrainerWorkloadProcessedEvent;
import com.epam.gymcrm.workload.repository.TrainerWorkloadProcessedEventRepository;
import com.epam.gymcrm.workload.repository.TrainerWorkloadRepository;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TrainerWorkloadMongoIntegrationTest {

  private static final int MONGO_PORT = 27017;
  private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:7");

  @Container
  private static final GenericContainer<?> mongo = new GenericContainer<>(MONGO_IMAGE)
      .withExposedPorts(MONGO_PORT)
      .withCommand("mongod", "--replSet", "rs0", "--bind_ip_all");

  @Autowired private MongoTemplate mongoTemplate;

  @Autowired private TrainerWorkloadRepository trainerWorkloadRepository;

  @Autowired private TrainerWorkloadProcessedEventRepository processedEventRepository;

  @Autowired private TrainerWorkloadService trainerWorkloadService;

  @Autowired private MeterRegistry meterRegistry;

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mongodb.uri", () -> "mongodb://"
        + mongo.getHost()
        + ":"
        + mongo.getMappedPort(MONGO_PORT)
        + "/trainer_workload_it?directConnection=true&replicaSet=rs0");
  }

  @BeforeAll
  static void initializeReplicaSet() throws IOException, InterruptedException {
    mongo.execInContainer(
        "mongosh",
        "--quiet",
        "--eval",
        "try { rs.status().ok } catch (e) { "
            + "rs.initiate({_id: 'rs0', members: [{ _id: 0, host: 'localhost:27017' }]}) }");

    for (int attempt = 0; attempt < 30; attempt++) {
      ExecResult result = mongo.execInContainer(
          "mongosh",
          "--quiet",
          "--eval",
          "db.hello().isWritablePrimary ? 1 : 0");
      if (result.getStdout().trim().equals("1")) {
        return;
      }
      Thread.sleep(1000);
    }

    throw new IllegalStateException("MongoDB replica set did not elect a primary");
  }

  @BeforeEach
  void cleanCollections() {
    trainerWorkloadRepository.deleteAll();
    processedEventRepository.deleteAll();
  }

  @Test
  void shouldCreateRequiredMongoIndexes() {
    Set<String> workloadIndexNames = indexNames(TrainerWorkload.class);
    Set<String> processedEventIndexNames = indexNames(TrainerWorkloadProcessedEvent.class);

    assertThat(workloadIndexNames)
        .contains("idx_trainer_workloads_first_name_last_name");
    assertThat(processedEventIndexNames)
        .contains("uk_trainer_workload_processed_events_training_action");
  }

  @Test
  void shouldEnforceUniqueProcessedEventIndex() {
    TrainerWorkloadProcessedEvent processedEvent =
        TrainerWorkloadProcessedEvent.fromRequest(9001L, ActionType.ADD, Instant.now());

    processedEventRepository.insert(processedEvent);

    assertThatThrownBy(() -> processedEventRepository.insert(
        TrainerWorkloadProcessedEvent.fromRequest(9001L, ActionType.ADD, Instant.now())))
        .isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void shouldIgnoreDuplicateEventAndIncrementDuplicateCounter() {
    TrainerWorkloadRequest request = request(9002L, ActionType.ADD, 30);
    final double duplicateCountBefore = duplicateCounter(ActionType.ADD);

    trainerWorkloadService.updateTrainerWorkload(request);
    trainerWorkloadService.updateTrainerWorkload(request);

    TrainerWorkload trainer = trainerWorkloadRepository.findByUsername("Integration.Trainer")
        .orElseThrow();
    assertThat(trainer.getYears()).hasSize(1);
    assertThat(trainer.getYears().getFirst().getMonths()).hasSize(1);
    assertThat(trainer.getYears().getFirst().getMonths().getFirst()
        .getTrainingsSummaryDuration()).isEqualTo(30);
    assertThat(processedEventsCount(9002L, ActionType.ADD)).isEqualTo(1);
    assertThat(duplicateCounter(ActionType.ADD)).isEqualTo(duplicateCountBefore + 1);
  }

  @Test
  void shouldRollbackProcessedEventWhenWorkloadUpdateFails() {
    TrainerWorkloadRequest request = request(9003L, ActionType.DELETE, 15);

    assertThatThrownBy(() -> trainerWorkloadService.updateTrainerWorkload(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Training summary duration cannot be negative");

    assertThat(processedEventsCount(9003L, ActionType.DELETE)).isZero();
    assertThat(trainerWorkloadRepository.findByUsername("Integration.Trainer")).isEmpty();
  }

  private Set<String> indexNames(Class<?> documentClass) {
    return mongoTemplate.indexOps(documentClass).getIndexInfo().stream()
        .map(IndexInfo::getName)
        .collect(Collectors.toSet());
  }

  private long processedEventsCount(Long trainingId, ActionType actionType) {
    return mongoTemplate.count(query(where("trainingId").is(trainingId)
        .and("actionType").is(actionType)), TrainerWorkloadProcessedEvent.class);
  }

  private double duplicateCounter(ActionType actionType) {
    Counter counter = meterRegistry.find("trainer.workload.duplicate.events")
        .tag("actionType", actionType.name())
        .counter();
    return counter == null ? 0 : counter.count();
  }

  private static TrainerWorkloadRequest request(
      Long trainingId,
      ActionType actionType,
      int trainingDuration
  ) {
    return new TrainerWorkloadRequest(
        trainingId,
        "Integration.Trainer",
        "Integration",
        "Trainer",
        true,
        LocalDate.of(2026, 8, 10),
        trainingDuration,
        actionType);
  }
}
