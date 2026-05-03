package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.Main;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Main.class)
@Transactional
class TrainingDaoImplTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private TrainingDao trainingDao;

    @Test
    void saveShouldPersistTrainingAndFindByIdShouldReturnIt() {
        Training training = persistedTrainingGraph();

        trainingDao.save(training);
        entityManager.flush();
        entityManager.clear();

        Optional<Training> found = trainingDao.findById(training.getTrainingId());

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getTrainingName()).isEqualTo("Yoga Basics"),
                () -> assertThat(found.get().getTrainingDuration()).isEqualTo(60),
                () -> assertThat(found.get().getTrainee().getUser().getUsername()).isEqualTo("Training.Trainee"),
                () -> assertThat(found.get().getTrainer().getUser().getUsername()).isEqualTo("Training.Trainer"),
                () -> assertThat(found.get().getTrainingType().getTrainingTypeName()).isEqualTo("Yoga")
        );
    }

    @Test
    void saveShouldMergeExistingTraining() {
        Training training = persistedTrainingGraph();
        entityManager.persist(training);
        entityManager.flush();
        entityManager.clear();

        training.setTrainingName("Updated Yoga");
        training.setTrainingDuration(90);
        trainingDao.save(training);
        entityManager.flush();
        entityManager.clear();

        Optional<Training> found = trainingDao.findById(training.getTrainingId());

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getTrainingName()).isEqualTo("Updated Yoga"),
                () -> assertThat(found.get().getTrainingDuration()).isEqualTo(90)
        );
    }

    @Test
    void findByIdShouldReturnEmptyOptionalWhenTrainingDoesNotExist() {
        Optional<Training> found = trainingDao.findById(-1L);

        assertThat(found).isEmpty();
    }

    private Training persistedTrainingGraph() {
        Trainee trainee = trainee("Training", "Trainee", "Training.Trainee");
        Trainer trainer = trainer("Training", "Trainer", "Training.Trainer");
        TrainingType trainingType = trainingType("Yoga");

        entityManager.persist(trainee);
        entityManager.persist(trainer);
        entityManager.persist(trainingType);

        return training(trainee, trainer, trainingType);
    }
}
