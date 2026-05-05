package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class TrainerDaoImplTest extends PostgresContainerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private TrainerDao trainerDao;

    @Test
    void saveShouldPersistTrainerAndFindByIdShouldReturnIt() {
        Trainer trainer = trainer("Alice", "Brown", "Alice.Brown");

        trainerDao.save(trainer);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainer> found = trainerDao.findById(trainer.getId());

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getUser().getFirstName()).isEqualTo("Alice"),
                () -> assertThat(found.get().getUser().getUsername()).isEqualTo("Alice.Brown"),
                () -> assertThat(found.get().getSpecialization()).isEqualTo("Fitness")
        );
    }

    @Test
    void saveShouldMergeExistingTrainer() {
        Trainer trainer = trainer("Bob", "Smith", "Bob.Smith");
        entityManager.persist(trainer);
        entityManager.flush();
        entityManager.clear();

        trainer.setSpecialization("Cardio");
        trainerDao.save(trainer);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainer> found = trainerDao.findById(trainer.getId());

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Trainer::getSpecialization)
                .isEqualTo("Cardio");
    }

    @Test
    void findByIdShouldReturnEmptyOptionalWhenTrainerDoesNotExist() {
        Optional<Trainer> found = trainerDao.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void findByUsernameShouldReturnTrainerWhenUsernameExists() {
        Trainer trainer = trainer("Petro", "Fitness", "Petro.Fitness");
        entityManager.persist(trainer);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainer> found = trainerDao.findByUsername("Petro.Fitness");

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getUser().getUsername()).isEqualTo("Petro.Fitness"),
                () -> assertThat(found.get().getUser().getFirstName()).isEqualTo("Petro")
        );
    }

    @Test
    void findByUsernameShouldReturnEmptyOptionalWhenUsernameDoesNotExist() {
        Optional<Trainer> found = trainerDao.findByUsername("Unknown.Trainer");

        assertThat(found).isEmpty();
    }

    @Test
    void findByIdShouldReturnAssignedTraineesFromManyToManyRelation() {
        Trainer trainer = trainer("Relation", "Trainer", "Relation.Trainer");
        entityManager.persist(trainer);

        Trainee trainee = trainee("Relation", "Trainee", "Relation.Trainee");
        trainee.getTrainers().add(trainer);
        entityManager.persist(trainee);
        entityManager.flush();
        entityManager.clear();

        Optional<Trainer> found = trainerDao.findById(trainer.getId());

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Trainer::getTrainees)
                .satisfies(trainees -> assertThat(trainees)
                        .extracting(assignedTrainee -> assignedTrainee.getUser().getUsername())
                        .containsExactly("Relation.Trainee"));
    }

    @Test
    void findNotAssignedToTraineeShouldReturnActiveTrainersNotAssignedToTrainee() {
        Trainer assignedTrainer = trainer("Assigned", "Trainer", "Assigned.Trainer");
        Trainer unassignedTrainer = trainer("Available", "Trainer", "Available.Trainer");
        Trainer inactiveTrainer = trainer("Inactive", "Trainer", "Inactive.Trainer");
        inactiveTrainer.getUser().setActive(false);
        entityManager.persist(assignedTrainer);
        entityManager.persist(unassignedTrainer);
        entityManager.persist(inactiveTrainer);

        Trainee trainee = trainee("Target", "Trainee", "Target.Trainee");
        trainee.getTrainers().add(assignedTrainer);
        entityManager.persist(trainee);
        entityManager.flush();
        entityManager.clear();

        List<Trainer> result = trainerDao.findNotAssignedToTrainee("Target.Trainee");

        assertThat(result)
                .extracting(trainer -> trainer.getUser().getUsername())
                .containsExactly("Available.Trainer");
    }

    @Test
    void findNotAssignedToTraineeShouldReturnEmptyListWhenTraineeDoesNotExist() {
        Trainer trainer = trainer("Available", "Trainer", "Available.Trainer");
        entityManager.persist(trainer);
        entityManager.flush();
        entityManager.clear();

        List<Trainer> result = trainerDao.findNotAssignedToTrainee("Unknown.Trainee");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteShouldRemoveTrainerById() {
        Trainer trainer = trainer("Carol", "White", "Carol.White");
        entityManager.persist(trainer);
        entityManager.flush();
        Long id = trainer.getId();

        trainerDao.delete(id);
        entityManager.flush();
        entityManager.clear();

        assertThat(trainerDao.findById(id)).isEmpty();
    }

    @Test
    void deleteShouldDoNothingWhenTrainerDoesNotExist() {
        trainerDao.delete(-1L);
        entityManager.flush();

        assertThat(trainerDao.findById(-1L)).isEmpty();
    }

    @Test
    void findAllShouldReturnStoredTrainers() {
        Trainer first = trainer("Diana", "Green", "Diana.Green");
        Trainer second = trainer("Ethan", "Black", "Ethan.Black");
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();
        entityManager.clear();

        List<Trainer> all = trainerDao.findAll();

        assertThat(all)
                .extracting(trainer -> trainer.getUser().getUsername())
                .contains("Diana.Green", "Ethan.Black");
    }
}
