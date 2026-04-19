package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainerServiceTest {
    private  TrainerService trainerService;
    private TrainerDao trainerDao;

    @BeforeEach
    void setUp() {
        trainerDao = mock(TrainerDao.class);
        trainerService = new TrainerService();
        trainerService.setTrainerDao(trainerDao);
    }

    @Test
    void testCreateTrainerGeneratesUsernameAndPassword() {
        Trainer trainer = Trainer.builder()
                .firstName("Albus")
                .lastName("Dumbledore")
                .build();
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());

        trainerService.create(trainer);

        assertEquals("Albus.Dumbledore", trainer.getUsername());
        assertNotNull(trainer.getPassword());
        assertEquals(10, trainer.getPassword().length());
        verify(trainerDao, times(1)).save(trainer);

    }

    @Test
    void testGenerateUsernameFillsTheGapForTrainer() {
        Trainer newTrainer = Trainer.builder().firstName("Severus").lastName("Snape").build();
        Trainer existingBase = Trainer.builder().username("Severus.Snape").build();
        Trainer existing2 = Trainer.builder().username("Severus.Snape2").build();
        Trainer similarName = Trainer.builder().username("Severus.Snapeson").build();

        when(trainerDao.findAll()).thenReturn(List.of(existingBase, existing2, similarName));

        trainerService.create(newTrainer);

        assertEquals("Severus.Snape1", newTrainer.getUsername());
    }

    @Test
    void testfindById() {
        Trainer trainer = Trainer.builder().userId(10L).firstName("Hermiona").build();

        when(trainerDao.findById(10L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.findById(10L);

        assertTrue(result.isPresent());
        assertEquals("Hermiona", result.get().getFirstName());
    }
}
