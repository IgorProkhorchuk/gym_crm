package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainingServiceImplTest {

    private TrainingService trainingService;
    private TrainingDao trainingDao;

    @BeforeEach
    void setUp() {
        trainingDao = mock(TrainingDao.class);

        TrainingServiceImpl serviceImpl = new TrainingServiceImpl();
        serviceImpl.setTrainingDao(trainingDao);
        trainingService = serviceImpl;
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
    void testFindById() {
        Training training = Training.builder().trainingId(5L).trainingName("Yoga").build();
        when(trainingDao.findById(5L)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.findById(5L);

        assertTrue(result.isPresent());
        assertEquals("Yoga", result.get().getTrainingName());
        verify(trainingDao, times(1)).findById(5L);
    }

    @Test
    void testFindByIdNotFound() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Training> result = trainingService.findById(99L);

        assertFalse(result.isPresent());
        verify(trainingDao, times(1)).findById(99L);
    }
}