package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryStorageTest {

    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
        storage.setTrainees(new HashMap<>());
        storage.setTrainers(new HashMap<>());
        storage.setTrainings(new HashMap<>());
        storage.setTrainingTypes(new HashMap<>());
    }

    @Test
    void testGetStorageForTrainee() {
        Map<Long, Trainee> traineeMap = storage.getStorage(Trainee.class);
        assertNotNull(traineeMap);
    }

    @Test
    void testGetStorageForTrainer() {
        Map<Long, Trainer> trainerMap = storage.getStorage(Trainer.class);
        assertNotNull(trainerMap);
    }

    @Test
    void testGetStorageForTraining() {
        Map<Long, Training> trainingMap = storage.getStorage(Training.class);
        assertNotNull(trainingMap);
    }

    @Test
    void testGetStorageForUnknownClassThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            storage.getStorage(String.class)
        );

        assertTrue(exception.getMessage().contains("Unknown entity type"));
    }

    @Test
    void testGetStorageForTrainingType() {
        Map<Long, TrainingType> trainingTypeMap = storage.getStorage(TrainingType.class);
        assertNotNull(trainingTypeMap);
    }
}
