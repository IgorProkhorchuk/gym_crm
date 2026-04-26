package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryStorageTest {

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
        assertThat(traineeMap).isNotNull();
    }

    @Test
    void testGetStorageForTrainer() {
        Map<Long, Trainer> trainerMap = storage.getStorage(Trainer.class);
        assertThat(trainerMap).isNotNull();
    }

    @Test
    void testGetStorageForTraining() {
        Map<Long, Training> trainingMap = storage.getStorage(Training.class);
        assertThat(trainingMap).isNotNull();
    }

    @Test
    void testGetStorageForUnknownClassThrowsException() {
        assertThatThrownBy(() -> storage.getStorage(String.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown entity type");
    }

    @Test
    void testGetStorageForNullClassThrowsException() {
        assertThatThrownBy(() -> storage.getStorage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entity class must not be null");
    }

    @Test
    void testGetStorageForTrainingType() {
        Map<Long, TrainingType> trainingTypeMap = storage.getStorage(TrainingType.class);
        assertThat(trainingTypeMap).isNotNull();
    }
}
