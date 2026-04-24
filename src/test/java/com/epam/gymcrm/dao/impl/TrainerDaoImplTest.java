package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.model.Trainer;
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

public class TrainerDaoImplTest {

    @InjectMocks
    private TrainerDaoImpl trainerDao;

    @Mock
    private InMemoryStorage storage;

    private Map<Long, Trainer> trainerMap;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainerMap = new HashMap<>();
        when(storage.getStorage(Trainer.class)).thenReturn(trainerMap);
    }

    @Test
    void testSaveAndFindById() {
        Trainer trainer = Trainer.builder()
                .userId(1L)
                .firstName("Alice")
                .specialization("Fitness")
                .build();

        trainerDao.save(trainer);

        Optional<Trainer> found = trainerDao.findById(1L);
        assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals("Alice", found.get().getFirstName()),
                () -> assertEquals("Fitness", found.get().getSpecialization())
        );
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Trainer> found = trainerDao.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void testDelete() {
        Trainer trainer = Trainer.builder().userId(2L).firstName("Bob").build();
        trainerDao.save(trainer);

        trainerDao.delete(2L);

        Optional<Trainer> found = trainerDao.findById(2L);
        assertFalse(found.isPresent(), "Trainer should be removed from storage");
    }

    @Test
    void testFindAll() {
        Trainer t1 = Trainer.builder().userId(10L).firstName("Trainer1").build();
        Trainer t2 = Trainer.builder().userId(20L).firstName("Trainer2").build();

        trainerDao.save(t1);
        trainerDao.save(t2);

        List<Trainer> all = trainerDao.findAll();

        assertAll(
                () -> assertEquals(2, all.size()),
                () -> assertTrue(all.contains(t1)),
                () -> assertTrue(all.contains(t2))
        );
    }
}
