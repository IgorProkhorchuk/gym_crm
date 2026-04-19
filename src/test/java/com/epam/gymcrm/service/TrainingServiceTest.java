package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    private TrainingService trainingService;
    private TrainingDao trainingDao;

    @BeforeEach
    void setUp() {
        trainingDao = mock(TrainingDao.class);
        trainingService = new TrainingService();
        trainingService.setTrainingDao(trainingDao);
    }

    @Test
    void testCreateTraining() {
        Training training = Training.builder()
                .trainingId(1L)
                .trainingName("Cardio Basics")
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
}