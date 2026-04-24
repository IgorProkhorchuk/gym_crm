package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TraineeDaoImplTest {

    private TraineeDao traineeDao;
    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
        storage.setTrainees(new HashMap<>());

        traineeDao = new TraineeDaoImpl();
        traineeDao.setStorage(storage);
    }

    @Test
    void testSaveAndFindById() {
        Trainee trainee = Trainee.builder().userId(1L).firstName("Oleg").build();

        traineeDao.save(trainee);

        Optional<Trainee> found = traineeDao.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("Oleg", found.get().getFirstName());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Trainee> found = traineeDao.findById(99L);
        assertFalse(found.isPresent());
    }

    @Test
    void testDelete() {
        Trainee trainee = Trainee.builder().userId(2L).firstName("Ivan").build();
        traineeDao.save(trainee);

        traineeDao.delete(2L);

        Optional<Trainee> found = traineeDao.findById(2L);
        assertFalse(found.isPresent(), "Trainee should be deleted");
    }

    @Test
    void testFindAll() {
        Trainee trainee1 = Trainee.builder().userId(3L).firstName("Anna").build();
        Trainee trainee2 = Trainee.builder().userId(4L).firstName("Maria").build();

        traineeDao.save(trainee1);
        traineeDao.save(trainee2);

        List<Trainee> all = traineeDao.findAll();

        assertEquals(2, all.size());
        assertTrue(all.contains(trainee1));
        assertTrue(all.contains(trainee2));
    }
}
