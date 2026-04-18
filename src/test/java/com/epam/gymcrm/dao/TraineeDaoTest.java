package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TraineeDaoTest {

    private TraineeDao traineeDao;
    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();

        storage.setTrainees(new HashMap<>());

        traineeDao = new TraineeDao();
        traineeDao.setStorage(storage);
    }

    @Test
    void testSaveAndFind() {
        Trainee trainee = Trainee.builder()
                .userId(10L)
                .firstName("Oleg")
                .build();

        traineeDao.save(trainee);

        Optional<Trainee> found = traineeDao.findById(10L);
        assertTrue(found.isPresent());
        assertEquals("Oleg", found.get().getFirstName());
    }
}