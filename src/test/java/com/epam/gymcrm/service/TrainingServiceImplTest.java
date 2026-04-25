package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainingServiceImplTest {

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Mock
    private TrainingDao trainingDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTraining() {
        Training training = Training.builder()
                .trainingId(1L)
                .trainingName("Cardio")
                .build();

        trainingService.create(training);

        verify(trainingDao, times(1)).save(training);
    }

    @Test
    void testCreateTrainingRethrowsDaoFailure() {
        Training training = Training.builder().trainingId(1L).build();
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(trainingDao).save(training);

        RuntimeException result = assertThrows(RuntimeException.class, () -> trainingService.create(training));

        assertSame(exception, result);
    }

    @Test
    void testFindById() {
        Training training = Training.builder().trainingId(5L).trainingName("Yoga").build();
        when(trainingDao.findById(5L)).thenReturn(Optional.of(training));

        Training result = trainingService.findById(5L);

        assertAll(
                () -> assertSame(training, result),
                () -> assertEquals("Yoga", result.getTrainingName()),
                () -> verify(trainingDao, times(1)).findById(5L)
        );
    }

    @Test
    void testFindByIdThrowsWhenNotFound() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException result = assertThrows(
                EntityNotFoundException.class,
                () -> trainingService.findById(99L)
        );

        assertAll(
                () -> assertEquals("Training not found", result.getMessage()),
                () -> verify(trainingDao, times(1)).findById(99L)
        );
    }

    @Test
    void testFindTrainingByIdRethrowsDaoFailure() {
        RuntimeException exception = new RuntimeException("DAO failure");
        when(trainingDao.findById(99L)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> trainingService.findById(99L));

        assertSame(exception, result);
    }

    @Test
    void testCreateTrainingRejectsNullTraining() {
        IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.create(null)
        );

        assertEquals("Training must not be null", result.getMessage());
    }

    @Test
    void testFindTrainingByIdRejectsNullId() {
        IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.findById(null)
        );

        assertEquals("Training id must not be null", result.getMessage());
    }
}
