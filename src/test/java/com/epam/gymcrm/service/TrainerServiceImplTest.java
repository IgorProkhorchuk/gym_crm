package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainerServiceImplTest {

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTrainerGeneratesUsernameAndPassword() {
        Trainer trainer = Trainer.builder().firstName("Severus").lastName("Snape").build();
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());

        trainerService.create(trainer);

        assertAll(
                () -> assertEquals("Severus.Snape", trainer.getUsername()),
                () -> assertEquals("Passw0rd12", trainer.getPassword()),
                () -> verify(passwordGenerator, times(1)).generate(),
                () -> verify(trainerDao, times(1)).save(trainer)
        );
    }

    @Test
    void testCreateTrainerWithExistingUsernameGeneratesSuffix() {
        Trainer newTrainer = Trainer.builder().firstName("Severus").lastName("Snape").build();
        Trainer existingTrainer = Trainer.builder().username("Severus.Snape").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));

        trainerService.create(newTrainer);

        assertEquals("Severus.Snape1", newTrainer.getUsername());
    }

    @Test
    void testCreateTrainerFillsTheGapInUsernames() {
        Trainer newTrainer = Trainer.builder().firstName("Severus").lastName("Snape").build();

        Trainer existingBase = Trainer.builder().username("Severus.Snape").build();
        Trainer existing2 = Trainer.builder().username("Severus.Snape2").build();
        Trainer similarName = Trainer.builder().username("Severus.Snapely").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(List.of(existingBase, existing2, similarName));

        trainerService.create(newTrainer);

        assertEquals("Severus.Snape1", newTrainer.getUsername());
    }

    @Test
    void testFindByIdReturnsTrainer() {
        Trainer trainer = Trainer.builder().userId(50L).firstName("Minerva").build();
        when(trainerDao.findById(50L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.findById(50L);

        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals("Minerva", result.get().getFirstName()),
                () -> verify(trainerDao, times(1)).findById(50L)
        );
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.findById(99L);

        assertAll(
                () -> assertFalse(result.isPresent()),
                () -> verify(trainerDao, times(1)).findById(99L)
        );
    }

    @Test
    void testCreateTrainerRethrowsDaoFailure() {
        Trainer trainer = Trainer.builder().userId(30L).firstName("Severus").lastName("Snape").build();
        RuntimeException exception = new RuntimeException("DAO failure");

        when(trainerDao.findAll()).thenReturn(Collections.emptyList());
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        doThrow(exception).when(trainerDao).save(trainer);

        RuntimeException result = assertThrows(RuntimeException.class, () -> trainerService.create(trainer));

        assertSame(exception, result);
    }

    @Test
    void testCreateTrainerSkipsTakenSequentialSuffixes() {
        Trainer newTrainer = Trainer.builder()
                .firstName("Severus")
                .lastName("Snape")
                .build();

        Trainer existingBase = Trainer.builder().username("Severus.Snape").build();
        Trainer existing1 = Trainer.builder().username("Severus.Snape1").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(List.of(existingBase, existing1));

        trainerService.create(newTrainer);

        assertAll(
                () -> assertEquals("Severus.Snape2", newTrainer.getUsername()),
                () -> assertEquals("Passw0rd12", newTrainer.getPassword()),
                () -> verify(passwordGenerator, times(1)).generate(),
                () -> verify(trainerDao, times(1)).save(newTrainer)
        );
    }

    @Test
    void testUpdateTrainerDelegatesToDao() {
        Trainer trainer = Trainer.builder()
                .userId(22L)
                .firstName("Minerva")
                .lastName("McGonagall")
                .username("Minerva.McGonagall")
                .build();

        trainerService.update(trainer);

        assertAll(
                () -> verify(trainerDao, times(1)).save(trainer),
                () -> verifyNoMoreInteractions(trainerDao)
        );
    }

    @Test
    void testUpdateTrainerRethrowsDaoFailure() {
        Trainer trainer = Trainer.builder().userId(22L).build();
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(trainerDao).save(trainer);

        RuntimeException result = assertThrows(RuntimeException.class, () -> trainerService.update(trainer));

        assertSame(exception, result);
    }

    @Test
    void testFindTrainerByIdRethrowsDaoFailure() {
        RuntimeException exception = new RuntimeException("DAO failure");
        when(trainerDao.findById(22L)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> trainerService.findById(22L));

        assertSame(exception, result);
    }

}
