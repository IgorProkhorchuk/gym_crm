package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TrainingDaoImplTest {

    private TrainingDao trainingDao;
    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
        storage.setTrainings(new HashMap<>());

        trainingDao = new TrainingDaoImpl();
        trainingDao.setStorage(storage);
    }

    @Test
    void testSaveAndFindById() {
        Training training = Training.builder()
                .trainingId(1L)
                .trainingName("Yoga Basics")
                .build();

        trainingDao.save(training);

        Optional<Training> found = trainingDao.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("Yoga Basics", found.get().getTrainingName());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Training> found = trainingDao.findById(99L);
        assertFalse(found.isPresent());
    }
}