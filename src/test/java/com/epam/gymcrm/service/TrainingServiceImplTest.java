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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Mock
    private TrainingDao trainingDao;

    @Test
    void createShouldSaveTraining() {
        Training training = Training.builder()
                .trainingId(1L)
                .trainingName("Cardio")
                .build();

        trainingService.create(training);

        verify(trainingDao).save(training);
    }

    @Test
    void createShouldThrowRuntimeExceptionWhenDaoFails() {
        Training training = Training.builder().trainingId(1L).build();
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(trainingDao).save(training);

        assertThatThrownBy(() -> trainingService.create(training))
                .isSameAs(exception);
    }

    @Test
    void findByIdShouldReturnTrainingWhenTrainingExists() {
        Training training = Training.builder().trainingId(5L).trainingName("Yoga").build();
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
    void createShouldThrowIllegalArgumentExceptionWhenTrainingIsNull() {
        assertThatThrownBy(() -> trainingService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training must not be null");
    }

    @Test
    void findByIdShouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> trainingService.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training id must not be null");
    }
}
