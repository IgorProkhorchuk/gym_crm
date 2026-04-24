package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TraineeDaoImplTest {

    @InjectMocks
    private TraineeDaoImpl traineeDao;

    @Mock
    private InMemoryStorage storage;

    private Map<Long, Trainee> traineeMap;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        traineeMap = new HashMap<>();
        when(storage.getStorage(Trainee.class)).thenReturn(traineeMap);
    }

    @Test
    void testSaveAndFindById() {
        Trainee trainee = Trainee.builder().userId(1L).firstName("Oleg").build();

        traineeDao.save(trainee);

        Optional<Trainee> found = traineeDao.findById(1L);
        assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals("Oleg", found.get().getFirstName()),
                () -> verify(storage, atLeastOnce()).getStorage(Trainee.class)
        );
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

        assertAll(
                () -> assertEquals(2, all.size()),
                () -> assertTrue(all.contains(trainee1)),
                () -> assertTrue(all.contains(trainee2))
        );
    }
}
