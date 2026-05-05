package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class TrainingDaoImplTest extends PostgresContainerTest {

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

    @Test
    void findByTraineeUsernameAndCriteriaShouldReturnMatchingTrainings() {
        Trainee targetTrainee = trainee("Target", "Trainee", "Target.Trainee");
        Trainee otherTrainee = trainee("Other", "Trainee", "Other.Trainee");
        Trainer johnTrainer = trainer("John", "Coach", "John.Coach");
        Trainer annTrainer = trainer("Ann", "Coach", "Ann.Coach");
        TrainingType yogaType = trainingType("Yoga");
        TrainingType boxingType = trainingType("Boxing");
        persistAll(targetTrainee, otherTrainee, johnTrainer, annTrainer, yogaType, boxingType);

        Training expected = persistTraining(
                targetTrainee,
                johnTrainer,
                yogaType,
                LocalDate.of(2026, 1, 10)
        );
        persistTraining(targetTrainee, johnTrainer, yogaType, LocalDate.of(2026, 3, 10));
        persistTraining(targetTrainee, annTrainer, yogaType, LocalDate.of(2026, 1, 12));
        persistTraining(targetTrainee, johnTrainer, boxingType, LocalDate.of(2026, 1, 13));
        persistTraining(otherTrainee, johnTrainer, yogaType, LocalDate.of(2026, 1, 10));
        entityManager.flush();
        entityManager.clear();

        List<Training> result = trainingDao.findByTraineeUsernameAndCriteria(
                "Target.Trainee",
                new TraineeTrainingCriteria(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31),
                        "john",
                        "Yoga"
                )
        );

        assertAll(
                () -> assertThat(result)
                        .extracting(Training::getTrainingId)
                        .containsExactly(expected.getTrainingId()),
                () -> assertThat(result.getFirst().getTrainer().getUser().getFirstName()).isEqualTo("John"),
                () -> assertThat(result.getFirst().getTrainingType().getTrainingTypeName()).isEqualTo("Yoga")
        );
    }

    @Test
    void findByTraineeUsernameAndCriteriaShouldUseEmptyCriteriaWhenCriteriaIsNull() {
        Trainee targetTrainee = trainee("Null", "Criteria", "Null.Criteria");
        Trainee otherTrainee = trainee("Other", "Criteria", "Other.Criteria");
        Trainer trainer = trainer("Null", "Trainer", "Null.Trainer");
        TrainingType trainingType = trainingType("Null Criteria Yoga");
        persistAll(targetTrainee, otherTrainee, trainer, trainingType);

        Training first = persistTraining(targetTrainee, trainer, trainingType, LocalDate.of(2026, 1, 10));
        Training second = persistTraining(targetTrainee, trainer, trainingType, LocalDate.of(2026, 2, 10));
        persistTraining(otherTrainee, trainer, trainingType, LocalDate.of(2026, 1, 10));
        entityManager.flush();
        entityManager.clear();

        List<Training> result = trainingDao.findByTraineeUsernameAndCriteria("Null.Criteria", null);

        assertThat(result)
                .extracting(Training::getTrainingId)
                .containsExactly(first.getTrainingId(), second.getTrainingId());
    }

    @Test
    void findByTraineeUsernameAndCriteriaShouldIgnoreBlankTextCriteria() {
        Trainee targetTrainee = trainee("Blank", "Criteria", "Blank.Criteria");
        Trainer firstTrainer = trainer("First", "Trainer", "First.Trainer");
        Trainer secondTrainer = trainer("Second", "Trainer", "Second.Trainer");
        TrainingType yogaType = trainingType("Blank Criteria Yoga");
        TrainingType boxingType = trainingType("Blank Criteria Boxing");
        persistAll(targetTrainee, firstTrainer, secondTrainer, yogaType, boxingType);

        Training first = persistTraining(targetTrainee, firstTrainer, yogaType, LocalDate.of(2026, 1, 10));
        Training second = persistTraining(targetTrainee, secondTrainer, boxingType, LocalDate.of(2026, 2, 10));
        entityManager.flush();
        entityManager.clear();

        List<Training> result = trainingDao.findByTraineeUsernameAndCriteria(
                "Blank.Criteria",
                new TraineeTrainingCriteria(null, null, " ", " ")
        );

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
        TrainingType yogaType = trainingType("Trainer Yoga");
        persistAll(aliceTrainee, bobTrainee, targetTrainer, otherTrainer, yogaType);

        Training expected = persistTraining(
                aliceTrainee,
                targetTrainer,
                yogaType,
                LocalDate.of(2026, 2, 10)
        );
        persistTraining(aliceTrainee, targetTrainer, yogaType, LocalDate.of(2026, 4, 10));
        persistTraining(bobTrainee, targetTrainer, yogaType, LocalDate.of(2026, 2, 10));
        persistTraining(aliceTrainee, otherTrainer, yogaType, LocalDate.of(2026, 2, 10));
        entityManager.flush();
        entityManager.clear();

        List<Training> result = trainingDao.findByTrainerUsernameAndCriteria(
                "Target.Trainer",
                new TrainerTrainingCriteria(
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 28),
                        "alice"
                )
        );

        assertAll(
                () -> assertThat(result)
                        .extracting(Training::getTrainingId)
                        .containsExactly(expected.getTrainingId()),
                () -> assertThat(result.getFirst().getTrainee().getUser().getFirstName()).isEqualTo("Alice"),
                () -> assertThat(result.getFirst().getTrainer().getUser().getUsername()).isEqualTo("Target.Trainer")
        );
    }

    @Test
    void findByTrainerUsernameAndCriteriaShouldUseEmptyCriteriaWhenCriteriaIsNull() {
        Trainee firstTrainee = trainee("First", "Runner", "First.Runner");
        Trainee secondTrainee = trainee("Second", "Runner", "Second.Runner");
        Trainer targetTrainer = trainer("Null", "Trainer", "Null.Criteria.Trainer");
        Trainer otherTrainer = trainer("Other", "Trainer", "Other.Criteria.Trainer");
        TrainingType trainingType = trainingType("Trainer Null Criteria Yoga");
        persistAll(firstTrainee, secondTrainee, targetTrainer, otherTrainer, trainingType);

        Training first = persistTraining(firstTrainee, targetTrainer, trainingType, LocalDate.of(2026, 1, 10));
        Training second = persistTraining(secondTrainee, targetTrainer, trainingType, LocalDate.of(2026, 2, 10));
        persistTraining(firstTrainee, otherTrainer, trainingType, LocalDate.of(2026, 1, 10));
        entityManager.flush();
        entityManager.clear();

        List<Training> result = trainingDao.findByTrainerUsernameAndCriteria("Null.Criteria.Trainer", null);

        assertThat(result)
                .extracting(Training::getTrainingId)
                .containsExactly(first.getTrainingId(), second.getTrainingId());
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

    private void persistAll(Object... entities) {
        for (Object entity : entities) {
            entityManager.persist(entity);
        }
    }

    private Training persistTraining(
            Trainee trainee,
            Trainer trainer,
            TrainingType trainingType,
            LocalDate trainingDate
    ) {
        Training training = training(trainee, trainer, trainingType);
        training.setTrainingDate(trainingDate);
        entityManager.persist(training);
        return training;
    }
}
