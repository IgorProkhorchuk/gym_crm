package com.epam.gymcrm.dao.impl;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TraineeDaoImplTest extends PostgresContainerTest {

  @PersistenceContext private EntityManager entityManager;

  @Resource private TraineeDao traineeDao;

  @Test
  void saveShouldPersistTraineeAndFindByIdShouldReturnIt() {
    Trainee trainee = trainee("Oleg", "Petrenko", "Oleg.Petrenko");

    traineeDao.save(trainee);
    entityManager.flush();
    entityManager.clear();

    Optional<Trainee> found = traineeDao.findById(trainee.getId());

    assertAll(
        () -> assertThat(found).isPresent(),
        () -> assertThat(found.get().getUser().getFirstName()).isEqualTo("Oleg"),
        () -> assertThat(found.get().getUser().getUsername()).isEqualTo("Oleg.Petrenko"),
        () -> assertThat(found.get().getAddress()).isEqualTo("Main Street, 123"));
  }

  @Test
  void saveShouldMergeExistingTrainee() {
    Trainee trainee = trainee("Ivan", "Franko", "Ivan.Franko");
    entityManager.persist(trainee);
    entityManager.flush();
    entityManager.clear();

    trainee.setAddress("Updated Street, 55");
    traineeDao.save(trainee);
    entityManager.flush();
    entityManager.clear();

    Optional<Trainee> found = traineeDao.findById(trainee.getId());

    assertThat(found)
        .isPresent()
        .get()
        .extracting(Trainee::getAddress)
        .isEqualTo("Updated Street, 55");
  }

  @Test
  void findByIdShouldReturnEmptyOptionalWhenTraineeDoesNotExist() {
    Optional<Trainee> found = traineeDao.findById(-1L);

    assertThat(found).isEmpty();
  }

  @Test
  void findByUsernameShouldReturnTraineeWhenUsernameExists() {
    Trainee trainee = trainee("Maria", "Shevchenko", "Maria.Shevchenko");
    entityManager.persist(trainee);
    entityManager.flush();
    entityManager.clear();

    Optional<Trainee> found = traineeDao.findByUsername("Maria.Shevchenko");

    assertAll(
        () -> assertThat(found).isPresent(),
        () -> assertThat(found.get().getUser().getUsername()).isEqualTo("Maria.Shevchenko"),
        () -> assertThat(found.get().getUser().getFirstName()).isEqualTo("Maria"));
  }

  @Test
  void findByUsernameShouldReturnEmptyOptionalWhenUsernameDoesNotExist() {
    Optional<Trainee> found = traineeDao.findByUsername("Unknown.Trainee");

    assertThat(found).isEmpty();
  }

  @Test
  void saveShouldPersistAssignedTrainersRelation() {
    Trainer trainer = trainer("Assigned", "Trainer", "Assigned.Trainer");
    persistSpecialization(trainer);
    entityManager.persist(trainer);

    Trainee trainee = trainee("Linked", "Trainee", "Linked.Trainee");
    trainee.getTrainers().add(trainer);

    traineeDao.save(trainee);
    entityManager.flush();
    entityManager.clear();

    Optional<Trainee> found = traineeDao.findById(trainee.getId());

    assertThat(found)
        .isPresent()
        .get()
        .extracting(Trainee::getTrainers)
        .satisfies(
            trainers ->
                assertThat(trainers)
                    .extracting(assignedTrainer -> assignedTrainer.getUser().getUsername())
                    .containsExactly("Assigned.Trainer"));
  }

  @Test
  void saveShouldReplaceAssignedTrainersRelation() {
    Trainer oldTrainer = trainer("Old", "Trainer", "Old.Trainer");
    Trainer newTrainer = trainer("New", "Trainer", "New.Trainer");
    persistSpecialization(oldTrainer);
    persistSpecialization(newTrainer);
    entityManager.persist(oldTrainer);
    entityManager.persist(newTrainer);

    Trainee trainee = trainee("Updated", "Trainee", "Updated.Trainee");
    trainee.getTrainers().add(oldTrainer);
    entityManager.persist(trainee);
    entityManager.flush();
    entityManager.clear();

    Trainee found = traineeDao.findByUsername("Updated.Trainee").orElseThrow();
    Trainer managedNewTrainer = entityManager.find(Trainer.class, newTrainer.getId());
    found.getTrainers().clear();
    found.getTrainers().add(managedNewTrainer);
    traineeDao.save(found);
    entityManager.flush();
    entityManager.clear();

    Optional<Trainee> updated = traineeDao.findById(trainee.getId());

    assertThat(updated)
        .isPresent()
        .get()
        .extracting(Trainee::getTrainers)
        .satisfies(
            trainers ->
                assertThat(trainers)
                    .extracting(assignedTrainer -> assignedTrainer.getUser().getUsername())
                    .containsExactly("New.Trainer"));
  }

  @Test
  void deleteShouldRemoveTraineeById() {
    Trainee trainee = trainee("Lesya", "Ukrainka", "Lesya.Ukrainka");
    entityManager.persist(trainee);
    entityManager.flush();
    Long id = trainee.getId();

    traineeDao.delete(id);
    entityManager.flush();
    entityManager.clear();

    assertThat(traineeDao.findById(id)).isEmpty();
  }

  @Test
  void deleteShouldCascadeRemoveRelevantTrainingsButKeepTrainer() {
    Trainee trainee = trainee("Cascade", "Trainee", "Cascade.Trainee");
    Trainer trainer = trainer("Cascade", "Trainer", "Cascade.Trainer");
    final TrainingType trainingType = findTrainingType("Fitness");

    persistSpecialization(trainer);
    entityManager.persist(trainer);
    trainee.getTrainers().add(trainer);
    entityManager.persist(trainee);

    Training training = training(trainee, trainer, trainingType);
    entityManager.persist(training);
    entityManager.flush();
    entityManager.clear();

    Long traineeId = trainee.getId();
    final Long trainerId = trainer.getId();
    final Long trainingId = training.getTrainingId();

    traineeDao.delete(traineeId);
    entityManager.flush();
    entityManager.clear();

    Number traineeTrainerCount =
        (Number)
            entityManager
                .createNativeQuery(
                    """
                        select count(*)
                        from trainees_trainers
                        where trainee_id = :traineeId
                    """)
                .setParameter("traineeId", traineeId)
                .getSingleResult();

    assertAll(
        () -> assertThat(entityManager.find(Trainee.class, traineeId)).isNull(),
        () -> assertThat(entityManager.find(Training.class, trainingId)).isNull(),
        () -> assertThat(traineeTrainerCount.longValue()).isZero(),
        () -> assertThat(entityManager.find(Trainer.class, trainerId)).isNotNull());
  }

  @Test
  void deleteShouldDoNothingWhenTraineeDoesNotExist() {
    traineeDao.delete(-1L);
    entityManager.flush();

    assertThat(traineeDao.findById(-1L)).isEmpty();
  }

  @Test
  void findAllShouldReturnStoredTrainees() {
    Trainee first = trainee("Anna", "Taylor", "Anna.Taylor");
    Trainee second = trainee("Brian", "Miller", "Brian.Miller");
    entityManager.persist(first);
    entityManager.persist(second);
    entityManager.flush();
    entityManager.clear();

    List<Trainee> all = traineeDao.findAll(PageRequest.firstPage());

    assertThat(all)
        .extracting(trainee -> trainee.getUser().getUsername())
        .contains("Anna.Taylor", "Brian.Miller");
  }

  @Test
  void findAllShouldApplyPageLimit() {
    entityManager.persist(trainee("Paged", "One", "Paged.One"));
    entityManager.persist(trainee("Paged", "Two", "Paged.Two"));
    entityManager.flush();
    entityManager.clear();

    List<Trainee> all = traineeDao.findAll(new PageRequest(0, 1));

    assertThat(all).hasSize(1);
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
}
