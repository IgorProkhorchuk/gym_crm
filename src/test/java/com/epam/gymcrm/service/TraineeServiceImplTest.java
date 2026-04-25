package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.service.impl.TraineeServiceImpl;
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

public class TraineeServiceImplTest {

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTraineeGeneratesUsernameAndPassword() {
        Trainee trainee = Trainee.builder().firstName("John").lastName("Doe").build();
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());

        traineeService.create(trainee);

        assertAll(
                () -> assertEquals("John.Doe", trainee.getUsername()),
                () -> assertEquals("Passw0rd12", trainee.getPassword()),
                () -> verify(passwordGenerator, times(1)).generate(),
                () -> verify(traineeDao, times(1)).save(trainee)
        );
    }

    @Test
    void testCreateTraineeWithExistingUsernameGeneratesSuffix() {
        Trainee newTrainee = Trainee.builder().firstName("John").lastName("Doe").build();
        Trainee existingTrainee = Trainee.builder().username("John.Doe").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee));

        traineeService.create(newTrainee);

        assertEquals("John.Doe1", newTrainee.getUsername());
    }

    @Test
    void testCreateTraineeFillsTheGapInUsernames() {
        Trainee newTrainee = Trainee.builder().firstName("John").lastName("Doe").build();

        Trainee existingBase = Trainee.builder().username("John.Doe").build();
        Trainee existing2 = Trainee.builder().username("John.Doe2").build();
        Trainee similarName = Trainee.builder().username("John.Doering").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(existingBase, existing2, similarName));

        traineeService.create(newTrainee);

        assertEquals("John.Doe1", newTrainee.getUsername());
    }

    @Test
    void testFindByIdReturnsTrainee() {
        Trainee trainee = Trainee.builder().userId(100L).firstName("Ron").build();
        when(traineeDao.findById(100L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.findById(100L);

        assertAll(
                () -> assertSame(trainee, result),
                () -> assertEquals("Ron", result.getFirstName()),
                () -> verify(traineeDao, times(1)).findById(100L)
        );
    }

    @Test
    void testFindByIdThrowsWhenNotFound() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException result = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.findById(999L)
        );

        assertAll(
                () -> assertEquals("Trainee profile not found", result.getMessage()),
                () -> verify(traineeDao, times(1)).findById(999L)
        );
    }

    @Test
    void testCreateTraineeRethrowsDaoFailure() {
        Trainee trainee = Trainee.builder().userId(20L).firstName("John").lastName("Doe").build();
        RuntimeException exception = new RuntimeException("DAO failure");

        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        doThrow(exception).when(traineeDao).save(trainee);

        RuntimeException result = assertThrows(RuntimeException.class, () -> traineeService.create(trainee));

        assertSame(exception, result);
    }

    @Test
    void testCreateTraineeRejectsNullTrainee() {
        IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.create(null)
        );

        assertEquals("Trainee must not be null", result.getMessage());
    }

    @Test
    void testCreateTraineeSkipsTakenSequentialSuffixes() {
        Trainee newTrainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        Trainee existingBase = Trainee.builder().username("John.Doe").build();
        Trainee existing1 = Trainee.builder().username("John.Doe1").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(existingBase, existing1));

        traineeService.create(newTrainee);

        assertAll(
                () -> assertEquals("John.Doe2", newTrainee.getUsername()),
                () -> assertEquals("Passw0rd12", newTrainee.getPassword()),
                () -> verify(passwordGenerator, times(1)).generate(),
                () -> verify(traineeDao, times(1)).save(newTrainee)
        );
    }

    @Test
    void testUpdateTraineeDelegatesToDao() {
        Trainee trainee = Trainee.builder()
                .userId(10L)
                .firstName("Hermione")
                .lastName("Granger")
                .username("Hermione.Granger")
                .build();

        traineeService.update(trainee);

        verify(traineeDao, times(1)).save(trainee);
        verifyNoMoreInteractions(traineeDao);
    }

    @Test
    void testUpdateTraineeRethrowsDaoFailure() {
        Trainee trainee = Trainee.builder().userId(10L).build();
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(traineeDao).save(trainee);

        RuntimeException result = assertThrows(RuntimeException.class, () -> traineeService.update(trainee));

        assertSame(exception, result);
    }

    @Test
    void testDeleteTraineeDelegatesToDao() {
        traineeService.delete(15L);

        verify(traineeDao, times(1)).delete(15L);
        verifyNoMoreInteractions(traineeDao);
    }

    @Test
    void testDeleteTraineeRethrowsDaoFailure() {
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(traineeDao).delete(15L);

        RuntimeException result = assertThrows(RuntimeException.class, () -> traineeService.delete(15L));

        assertSame(exception, result);
    }

    @Test
    void testFindTraineeByIdRethrowsDaoFailure() {
        RuntimeException exception = new RuntimeException("DAO failure");
        when(traineeDao.findById(15L)).thenThrow(exception);

        RuntimeException result = assertThrows(RuntimeException.class, () -> traineeService.findById(15L));

        assertSame(exception, result);
    }

    @Test
    void testFindTraineeByIdRejectsNullId() {
        IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.findById(null)
        );

        assertEquals("Trainee id must not be null", result.getMessage());
    }

}
