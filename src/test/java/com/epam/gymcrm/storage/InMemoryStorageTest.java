package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
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
        Map<Long, Trainee> map = storage.getStorage(Trainee.class);
        assertNotNull(map, "Shouldn't be null");
    }

    @Test
    void testSaveAndGetTrainee() {
        Trainee trainee = Trainee.builder()
                .userId(1L)
                .firstName("Ivan")
                .build();

        storage.getStorage(Trainee.class).put(trainee.getUserId(), trainee);

        Trainee saved = storage.getStorage(Trainee.class).get(1L);
        assertEquals("Ivan", saved.getFirstName());
    }
}