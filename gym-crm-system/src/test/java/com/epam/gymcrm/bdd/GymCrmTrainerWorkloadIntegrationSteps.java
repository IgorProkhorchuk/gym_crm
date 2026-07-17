package com.epam.gymcrm.bdd;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.client.workload.TrainerWorkloadActionType;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainerWorkloadOutboxEvent;
import com.epam.gymcrm.model.TrainerWorkloadOutboxStatus;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainerWorkloadOutboxRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.web.auth.JwtTokenService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public class GymCrmTrainerWorkloadIntegrationSteps {

  private final RestClient restClient;
  private final JwtTokenService jwtTokenService;
  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final TrainingTypeRepository trainingTypeRepository;
  private final TrainerWorkloadOutboxRepository outboxRepository;
  private int trainingResponseStatus;
  private String expectedTrainerUsername;
  private LocalDate expectedTrainingDate;
  private int expectedTrainingDuration;
  private TrainerWorkloadActionType expectedActionType;
  private long matchingOutboxEventsBeforeRequest;
  private TrainerWorkloadOutboxEvent outboxEvent;

  public GymCrmTrainerWorkloadIntegrationSteps(
      @LocalServerPort int port,
      JwtTokenService jwtTokenService,
      TraineeRepository traineeRepository,
      TrainerRepository trainerRepository,
      TrainingTypeRepository trainingTypeRepository,
      TrainerWorkloadOutboxRepository outboxRepository
  ) {
    this.restClient = RestClient.create("http://localhost:" + port);
    this.jwtTokenService = jwtTokenService;
    this.traineeRepository = traineeRepository;
    this.trainerRepository = trainerRepository;
    this.trainingTypeRepository = trainingTypeRepository;
    this.outboxRepository = outboxRepository;
  }

  @Given("an active trainee {string} and trainer {string} exist")
  public void activeTraineeAndTrainerExist(String traineeUsername, String trainerUsername) {
    TrainingType trainerSpecialization = trainingType("Fitness");
    traineeRepository
        .findByUsername(traineeUsername)
        .orElseGet(() -> traineeRepository.saveAndFlush(
            trainee("Integration", "Trainee", traineeUsername)));
    trainerRepository
        .findByUsername(trainerUsername)
        .orElseGet(() -> trainerRepository.saveAndFlush(
            trainer("Integration", "Trainer", trainerUsername, trainerSpecialization)));
  }

  @When(
      "the trainee {string} adds {string} training with trainer {string} on {string} "
          + "for {int} minutes")
  public void traineeAddsTraining(
      String traineeUsername,
      String trainingName,
      String trainerUsername,
      String trainingDate,
      int trainingDuration
  ) {
    expectedTrainerUsername = trainerUsername;
    expectedTrainingDate = LocalDate.parse(trainingDate);
    expectedTrainingDuration = trainingDuration;
    expectedActionType = TrainerWorkloadActionType.ADD;
    matchingOutboxEventsBeforeRequest = matchingOutboxEvents().size();

    String requestBody =
        """
        {
          "traineeUsername": "%s",
          "trainerUsername": "%s",
          "trainingName": "%s",
          "trainingTypeName": "Yoga",
          "trainingDate": "%s",
          "trainingDuration": %d
        }
        """
            .formatted(
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingDate,
                trainingDuration);

    try {
      ResponseEntity<Void> response =
          restClient
              .post()
              .uri("/api/v1/trainings")
              .contentType(MediaType.APPLICATION_JSON)
              .header(
                  HttpHeaders.AUTHORIZATION,
                  "Bearer " + jwtTokenService.createToken(traineeUsername, ProfileType.TRAINEE))
              .body(requestBody)
              .retrieve()
              .toBodilessEntity();
      trainingResponseStatus = response.getStatusCode().value();
    } catch (RestClientResponseException exception) {
      trainingResponseStatus = exception.getStatusCode().value();
    }
  }

  @Then("the training response status should be {int}")
  public void trainingResponseStatusShouldBe(int expectedStatus) {
    assertThat(trainingResponseStatus).isEqualTo(expectedStatus);
  }

  @Then("gym crm should create a pending trainer workload outbox event")
  public void gymCrmShouldCreatePendingTrainerWorkloadOutboxEvent() {
    List<TrainerWorkloadOutboxEvent> matchingEvents = matchingOutboxEvents();
    outboxEvent = matchingEvents.stream()
        .max(Comparator.comparing(TrainerWorkloadOutboxEvent::getId))
        .orElse(null);

    assertThat(outboxEvent).isNotNull();
    assertThat(outboxEvent.getStatus()).isEqualTo(TrainerWorkloadOutboxStatus.PENDING);
    assertThat(matchingEvents).hasSizeGreaterThan((int) matchingOutboxEventsBeforeRequest);
  }

  @Then(
      "the outbox event should contain trainer {string}, date {string}, duration {int} "
          + "and action {string}")
  public void outboxEventShouldContainTrainerDateDurationAndAction(
      String trainerUsername,
      String trainingDate,
      int trainingDuration,
      String actionType
  ) {
    assertThat(outboxEvent).isNotNull();
    assertThat(outboxEvent.getTrainerUsername()).isEqualTo(trainerUsername);
    assertThat(outboxEvent.getTrainerFirstName()).isEqualTo("Integration");
    assertThat(outboxEvent.getTrainerLastName()).isEqualTo("Trainer");
    assertThat(outboxEvent.getTrainerStatus()).isTrue();
    assertThat(outboxEvent.getTrainingDate()).isEqualTo(LocalDate.parse(trainingDate));
    assertThat(outboxEvent.getTrainingDuration()).isEqualTo(trainingDuration);
    assertThat(outboxEvent.getActionType()).isEqualTo(TrainerWorkloadActionType.valueOf(actionType));
  }

  private List<TrainerWorkloadOutboxEvent> matchingOutboxEvents() {
    return outboxRepository.findAll().stream()
        .filter(event -> expectedTrainerUsername.equals(event.getTrainerUsername()))
        .filter(event -> expectedTrainingDate.equals(event.getTrainingDate()))
        .filter(event -> expectedTrainingDuration == event.getTrainingDuration())
        .filter(event -> expectedActionType == event.getActionType())
        .toList();
  }

  private TrainingType trainingType(String name) {
    return trainingTypeRepository
        .findByName(name)
        .orElseThrow(() -> new IllegalStateException("Training type not found: " + name));
  }
}
