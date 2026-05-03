package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Mock
    private TrainingDao trainingDao;

    @Test
    void createShouldSaveTraining() {
        Training training = validTraining();

        trainingService.create(training);

        verify(trainingDao).save(training);
    }

    @Test
    void createShouldThrowRuntimeExceptionWhenDaoFails() {
        Training training = validTraining();
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(trainingDao).save(training);

        assertThatThrownBy(() -> trainingService.create(training))
                .isSameAs(exception);
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenTrainingIsNull() {
        assertThatThrownBy(() -> trainingService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training must not be null");
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenTraineeIsNull() {
        Training training = validTraining();
        training.setTrainee(null);

        assertThatThrownBy(() -> trainingService.create(training))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training trainee must not be null");
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenTrainerIsNull() {
        Training training = validTraining();
        training.setTrainer(null);

        assertThatThrownBy(() -> trainingService.create(training))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training trainer must not be null");
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenTrainingTypeIsNull() {
        Training training = validTraining();
        training.setTrainingType(null);

        assertThatThrownBy(() -> trainingService.create(training))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training type must not be null");
    }

    @Test
    void findByIdShouldReturnTrainingWhenTrainingExists() {
        Training training = validTraining();
        training.setTrainingId(5L);
        training.setTrainingName("Yoga");
        when(trainingDao.findById(5L)).thenReturn(Optional.of(training));

        Training result = trainingService.findById(5L);

        assertAll(
                () -> assertThat(result).isSameAs(training),
                () -> assertThat(result.getTrainingName()).isEqualTo("Yoga"),
                () -> verify(trainingDao).findById(5L)
        );
    }

    @Test
    void findByIdShouldThrowEntityNotFoundExceptionWhenTrainingDoesNotExist() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Training not found");

        verify(trainingDao).findById(99L);
    }

    @Test
    void findByIdShouldThrowRuntimeExceptionWhenDaoFails() {
        RuntimeException exception = new RuntimeException("DAO failure");
        when(trainingDao.findById(99L)).thenThrow(exception);

        assertThatThrownBy(() -> trainingService.findById(99L))
                .isSameAs(exception);
    }

    @Test
    void findByIdShouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> trainingService.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training id must not be null");
    }

    private static Training validTraining() {
        return training(
                trainee("Training", "Trainee", "Training.Trainee"),
                trainer("Training", "Trainer", "Training.Trainer"),
                trainingType("Yoga")
        );
    }
}
