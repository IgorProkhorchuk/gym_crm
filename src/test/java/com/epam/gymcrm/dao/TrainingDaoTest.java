package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainingDaoTest {
    private TrainingDao trainingDao;

    @BeforeEach
    void setup() {
        InMemoryStorage storage = new InMemoryStorage();
        storage.setTrainings(new HashMap<>());

        trainingDao = new TrainingDao();
        trainingDao.setStorage(storage);
    }

    @Test
    void testSaveAndFindTraining() {
        Training training = Training.builder()
                .trainingId(500L)
                .trainingName("Morning Yoga")
                .build();

        trainingDao.save(training);

        Optional<Training> found = trainingDao.findById(500L);
        assertTrue(found.isPresent());
        assertEquals("Morning Yoga", found.get().getTrainingName());
    }
}
