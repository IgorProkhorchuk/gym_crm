package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TrainerDaoTest {

    private TrainerDao trainerDao;

    @BeforeEach
    void setUp() {
        InMemoryStorage storage = new InMemoryStorage();
        storage.setTrainers(new HashMap<>());

        trainerDao = new TrainerDao();
        trainerDao.setStorage(storage);
    }

    @Test
    void testSaveAndFindTrainer() {
        Trainer trainer = Trainer.builder()
                .userId(1L)
                .firstName("Petro")
                .lastName("Petrov")
                .username("Petro.Petrov")
                .password("password123")
                .isActive(true)
                .specialization("Fitness")
                .build();

        trainerDao.save(trainer);

        Optional<Long> id = Optional.ofNullable(trainer.getUserId());
        assertTrue(id.isPresent(), "Id should be present");

        Optional<Trainer> found = trainerDao.findById(1L);

        assertTrue(found.isPresent(), "Trainer should be found in storage");
        assertEquals("Petro", found.get().getFirstName());
        assertEquals("Petrov", found.get().getLastName());
        assertEquals("Fitness", found.get().getSpecialization());

    }

    @Test
    void testDeleteTrainer() {
        Trainer trainer = Trainer.builder()
                .userId(2L)
                .firstName("Semen")
                .lastName("Semenov")
                .username("Semen.Semenov")
                .isActive(true)
                .password("password123")
                .specialization("Yoga")
                .build();
        trainerDao.save(trainer);

        trainerDao.delete(2L);

        Optional<Trainer> found = trainerDao.findById(2L);
        assertFalse(found.isPresent(), "Trainer supposed to be deleted");
    }
}
