package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainingDaoImplTest {

    @InjectMocks
    private TrainingDaoImpl trainingDao;

    @Mock
    private InMemoryStorage storage;

    private Map<Long, Training> trainingMap;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainingMap = new HashMap<>();
        when(storage.getStorage(Training.class)).thenReturn(trainingMap);
    }

    @Test
    void testSaveAndFindById() {
        Training training = Training.builder()
                .trainingId(1L)
                .trainingName("Yoga Basics")
                .build();

        trainingDao.save(training);

        Optional<Training> found = trainingDao.findById(1L);
        assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals("Yoga Basics", found.get().getTrainingName())
        );
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Training> found = trainingDao.findById(99L);
        assertFalse(found.isPresent());
    }
}
