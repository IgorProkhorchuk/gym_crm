package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TraineeServiceImplTest {

    private TraineeService traineeService;
    private TraineeDao traineeDao;

    @BeforeEach
    void setUp() {
        traineeDao = mock(TraineeDao.class);

        TraineeServiceImpl serviceImpl = new TraineeServiceImpl();
        serviceImpl.setTraineeDao(traineeDao);
        traineeService = serviceImpl;
    }

    @Test
    void testCreateTraineeGeneratesUsernameAndPassword() {
        Trainee trainee = Trainee.builder().firstName("John").lastName("Doe").build();
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());

        traineeService.create(trainee);

        assertEquals("John.Doe", trainee.getUsername());
        assertNotNull(trainee.getPassword());
        assertEquals(10, trainee.getPassword().length());
        verify(traineeDao, times(1)).save(trainee);
    }

    @Test
    void testCreateTraineeWithExistingUsernameGeneratesSuffix() {
        Trainee newTrainee = Trainee.builder().firstName("John").lastName("Doe").build();
        Trainee existingTrainee = Trainee.builder().username("John.Doe").build();

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

        when(traineeDao.findAll()).thenReturn(List.of(existingBase, existing2, similarName));

        traineeService.create(newTrainee);

        assertEquals("John.Doe1", newTrainee.getUsername());
    }

    @Test
    void testFindByIdReturnsTrainee() {
        Trainee trainee = Trainee.builder().userId(100L).firstName("Ron").build();
        when(traineeDao.findById(100L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.findById(100L);

        assertTrue(result.isPresent());
        assertEquals("Ron", result.get().getFirstName());
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.findById(999L);

        assertFalse(result.isPresent());
    }
}