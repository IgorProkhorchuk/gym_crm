package com.epam.gymcrm.repository;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrainingRepositoryTest extends PostgresContainerTest {

  @PersistenceContext private EntityManager entityManager;

  @Resource private TrainingRepository trainingRepository;

  @Test
  void saveShouldPersistTrainingAndFindByIdShouldReturnIt() {
    Training training = persistedTrainingGraph();

    trainingRepository.save(training);
    entityManager.flush();
    entityManager.clear();

    Optional<Training> found = trainingRepository.findById(training.getTrainingId());

    assertAll(
        () -> assertThat(found).isPresent(),
        () -> assertThat(found.get().getTrainingName()).isEqualTo("Yoga Basics"),
        () -> assertThat(found.get().getTrainingDuration()).isEqualTo(60),
        () ->
            assertThat(found.get().getTrainee().getUser().getUsername())
                .isEqualTo("Training.Trainee"),
        () ->
            assertThat(found.get().getTrainer().getUser().getUsername())
                .isEqualTo("Training.Trainer"),
        () -> assertThat(found.get().getTrainingType().getTrainingTypeName()).isEqualTo("Yoga"));
  }

  @Test
  void saveShouldMergeExistingTraining() {
    Training training = persistedTrainingGraph();
    entityManager.persist(training);
    entityManager.flush();
    entityManager.clear();

    training.setTrainingName("Updated Yoga");
    training.setTrainingDuration(90);
    trainingRepository.save(training);
    entityManager.flush();
    entityManager.clear();

    Optional<Training> found = trainingRepository.findById(training.getTrainingId());

    assertAll(
        () -> assertThat(found).isPresent(),
        () -> assertThat(found.get().getTrainingName()).isEqualTo("Updated Yoga"),
        () -> assertThat(found.get().getTrainingDuration()).isEqualTo(90));
  }

  @Test
  void findByIdShouldReturnEmptyOptionalWhenTrainingDoesNotExist() {
    Optional<Training> found = trainingRepository.findById(-1L);

    assertThat(found).isEmpty();
  }

  @Test
  void findByTraineeUsernameAndCriteriaShouldReturnMatchingTrainings() {
    Trainee targetTrainee = trainee("Target", "Trainee", "Target.Trainee");
    Trainee otherTrainee = trainee("Other", "Trainee", "Other.Trainee");
    Trainer johnTrainer = trainer("John", "Coach", "John.Coach");
    Trainer annTrainer = trainer("Ann", "Coach", "Ann.Coach");
    TrainingType yogaType = findTrainingType("Yoga");
    TrainingType boxingType = findTrainingType("Boxing");
    persistAll(targetTrainee, otherTrainee, johnTrainer, annTrainer, yogaType, boxingType);

    final Training expected =
        persistTraining(targetTrainee, johnTrainer, yogaType, LocalDate.of(2026, 1, 10));
    persistTraining(targetTrainee, johnTrainer, yogaType, LocalDate.of(2026, 3, 10));
    persistTraining(targetTrainee, annTrainer, yogaType, LocalDate.of(2026, 1, 12));
    persistTraining(targetTrainee, johnTrainer, boxingType, LocalDate.of(2026, 1, 13));
    persistTraining(otherTrainee, johnTrainer, yogaType, LocalDate.of(2026, 1, 10));
    entityManager.flush();
    entityManager.clear();

    List<Training> result =
        trainingRepository.findByTraineeUsernameAndCriteria(
            "Target.Trainee",
            new TraineeTrainingCriteria(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "john", "Yoga"),
            PageRequest.firstPage());

    assertAll(
        () ->
            assertThat(result)
                .extracting(Training::getTrainingId)
                .containsExactly(expected.getTrainingId()),
        () -> assertThat(result.getFirst().getTrainer().getUser().getFirstName()).isEqualTo("John"),
        () ->
            assertThat(result.getFirst().getTrainingType().getTrainingTypeName())
                .isEqualTo("Yoga"));
  }

  @Test
  void findByTraineeUsernameAndCriteriaShouldUseEmptyCriteriaWhenCriteriaIsNull() {
    Trainee targetTrainee = trainee("Null", "Criteria", "Null.Criteria");
    Trainee otherTrainee = trainee("Other", "Criteria", "Other.Criteria");
    Trainer trainer = trainer("Null", "Trainer", "Null.Trainer");
    TrainingType trainingType = findTrainingType("Yoga");
    persistAll(targetTrainee, otherTrainee, trainer, trainingType);

    final Training first =
        persistTraining(targetTrainee, trainer, trainingType, LocalDate.of(2026, 1, 10));
    final Training second =
        persistTraining(targetTrainee, trainer, trainingType, LocalDate.of(2026, 2, 10));
    persistTraining(otherTrainee, trainer, trainingType, LocalDate.of(2026, 1, 10));
    entityManager.flush();
    entityManager.clear();

    List<Training> result =
        trainingRepository.findByTraineeUsernameAndCriteria(
            "Null.Criteria", null, PageRequest.firstPage());

    assertThat(result)
        .extracting(Training::getTrainingId)
        .containsExactly(first.getTrainingId(), second.getTrainingId());
  }

  @Test
  void findByTraineeUsernameAndCriteriaShouldApplyPagination() {
    Trainee trainee = trainee("Paged", "Trainee", "Paged.Trainee");
    Trainer trainer = trainer("Paged", "Trainer", "Paged.Trainer");
    TrainingType trainingType = findTrainingType("Yoga");
    persistAll(trainee, trainer, trainingType);

    Training first = persistTraining(trainee, trainer, trainingType, LocalDate.of(2026, 1, 10));
    Training second = persistTraining(trainee, trainer, trainingType, LocalDate.of(2026, 2, 10));
    entityManager.flush();
    entityManager.clear();

    List<Training> result =
        trainingRepository.findByTraineeUsernameAndCriteria("Paged.Trainee", null, new PageRequest(1, 1));

    assertThat(result)
        .extracting(Training::getTrainingId)
        .containsExactly(second.getTrainingId())
        .doesNotContain(first.getTrainingId());
  }

  @Test
  void findByTraineeUsernameAndCriteriaShouldIgnoreBlankTextCriteria() {
    Trainee targetTrainee = trainee("Blank", "Criteria", "Blank.Criteria");
    Trainer firstTrainer = trainer("First", "Trainer", "First.Trainer");
    Trainer secondTrainer = trainer("Second", "Trainer", "Second.Trainer");
    TrainingType yogaType = findTrainingType("Yoga");
    TrainingType boxingType = findTrainingType("Boxing");
    persistAll(targetTrainee, firstTrainer, secondTrainer, yogaType, boxingType);

    Training first =
        persistTraining(targetTrainee, firstTrainer, yogaType, LocalDate.of(2026, 1, 10));
    Training second =
        persistTraining(targetTrainee, secondTrainer, boxingType, LocalDate.of(2026, 2, 10));
    entityManager.flush();
    entityManager.clear();

    List<Training> result =
        trainingRepository.findByTraineeUsernameAndCriteria(
            "Blank.Criteria",
            new TraineeTrainingCriteria(null, null, " ", " "),
            PageRequest.firstPage());

    assertThat(result)
        .extracting(Training::getTrainingId)
        .containsExactly(first.getTrainingId(), second.getTrainingId());
  }

  @Test
  void findByTrainerUsernameAndCriteriaShouldReturnMatchingTrainings() {
    Trainee aliceTrainee = trainee("Alice", "Runner", "Alice.Runner");
    Trainee bobTrainee = trainee("Bob", "Runner", "Bob.Runner");
    Trainer targetTrainer = trainer("Target", "Trainer", "Target.Trainer");
    Trainer otherTrainer = trainer("Other", "Trainer", "Other.Trainer");
    TrainingType yogaType = findTrainingType("Yoga");
    persistAll(aliceTrainee, bobTrainee, targetTrainer, otherTrainer, yogaType);

    final Training expected =
        persistTraining(aliceTrainee, targetTrainer, yogaType, LocalDate.of(2026, 2, 10));
    persistTraining(aliceTrainee, targetTrainer, yogaType, LocalDate.of(2026, 4, 10));
    persistTraining(bobTrainee, targetTrainer, yogaType, LocalDate.of(2026, 2, 10));
    persistTraining(aliceTrainee, otherTrainer, yogaType, LocalDate.of(2026, 2, 10));
    entityManager.flush();
    entityManager.clear();

    List<Training> result =
        trainingRepository.findByTrainerUsernameAndCriteria(
            "Target.Trainer",
            new TrainerTrainingCriteria(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), "alice"),
            PageRequest.firstPage());

    assertAll(
        () ->
            assertThat(result)
                .extracting(Training::getTrainingId)
                .containsExactly(expected.getTrainingId()),
        () ->
            assertThat(result.getFirst().getTrainee().getUser().getFirstName()).isEqualTo("Alice"),
        () ->
            assertThat(result.getFirst().getTrainer().getUser().getUsername())
                .isEqualTo("Target.Trainer"));
  }

  @Test
  void findByTrainerUsernameAndCriteriaShouldUseEmptyCriteriaWhenCriteriaIsNull() {
    Trainee firstTrainee = trainee("First", "Runner", "First.Runner");
    Trainee secondTrainee = trainee("Second", "Runner", "Second.Runner");
    Trainer targetTrainer = trainer("Null", "Trainer", "Null.Criteria.Trainer");
    Trainer otherTrainer = trainer("Other", "Trainer", "Other.Criteria.Trainer");
    TrainingType trainingType = findTrainingType("Yoga");
    persistAll(firstTrainee, secondTrainee, targetTrainer, otherTrainer, trainingType);

    final Training first =
        persistTraining(firstTrainee, targetTrainer, trainingType, LocalDate.of(2026, 1, 10));
    final Training second =
        persistTraining(secondTrainee, targetTrainer, trainingType, LocalDate.of(2026, 2, 10));
    persistTraining(firstTrainee, otherTrainer, trainingType, LocalDate.of(2026, 1, 10));
    entityManager.flush();
    entityManager.clear();

    List<Training> result =
        trainingRepository.findByTrainerUsernameAndCriteria(
            "Null.Criteria.Trainer", null, PageRequest.firstPage());

    assertThat(result)
        .extracting(Training::getTrainingId)
        .containsExactly(first.getTrainingId(), second.getTrainingId());
  }

  @Test
  void findByTrainerUsernameAndCriteriaShouldApplyPagination() {
    Trainee firstTrainee = trainee("Paged", "First", "Paged.First");
    Trainee secondTrainee = trainee("Paged", "Second", "Paged.Second");
    Trainer trainer = trainer("Paged", "Coach", "Paged.Coach");
    TrainingType trainingType = findTrainingType("Yoga");
    persistAll(firstTrainee, secondTrainee, trainer, trainingType);

    Training first =
        persistTraining(firstTrainee, trainer, trainingType, LocalDate.of(2026, 1, 10));
    Training second =
        persistTraining(secondTrainee, trainer, trainingType, LocalDate.of(2026, 2, 10));
    entityManager.flush();
    entityManager.clear();

    List<Training> result =
        trainingRepository.findByTrainerUsernameAndCriteria("Paged.Coach", null, new PageRequest(1, 1));

    assertThat(result)
        .extracting(Training::getTrainingId)
        .containsExactly(second.getTrainingId())
        .doesNotContain(first.getTrainingId());
  }

  private Training persistedTrainingGraph() {
    Trainee trainee = trainee("Training", "Trainee", "Training.Trainee");
    Trainer trainer = trainer("Training", "Trainer", "Training.Trainer");
    TrainingType trainingType = findTrainingType("Yoga");

    persistAll(trainee, trainer, trainingType);

    return training(trainee, trainer, trainingType);
  }

  private void persistAll(Object... entities) {
    for (Object entity : entities) {
      if (entityManager.contains(entity)) {
        continue;
      }
      if (entity instanceof Trainer trainer) {
        persistSpecialization(trainer);
      }
      entityManager.persist(entity);
    }
  }

  private void persistSpecialization(Trainer trainer) {
    TrainingType specialization = trainer.getSpecialization();
    if (specialization.getTrainingTypeId() == null) {
      trainer.setSpecialization(findTrainingType(specialization.getTrainingTypeName()));
    }
  }

  private TrainingType findTrainingType(String name) {
    return entityManager
        .createQuery(
            """
                select tt
                from TrainingType tt
                where tt.trainingTypeName = :name
            """,
            TrainingType.class)
        .setParameter("name", name)
        .getSingleResult();
  }

  private Training persistTraining(
      Trainee trainee, Trainer trainer, TrainingType trainingType, LocalDate trainingDate) {
    Training training = training(trainee, trainer, trainingType);
    training.setTrainingDate(trainingDate);
    entityManager.persist(training);
    return training;
  }
}
