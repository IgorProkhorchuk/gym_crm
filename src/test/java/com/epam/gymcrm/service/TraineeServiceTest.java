package com.epam.gymcrm.service;


import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class TraineeServiceTest {
    private TraineeService traineeService;
    private TraineeDao traineeDao;

    @BeforeEach
    void setUp() {
        traineeDao = mock(TraineeDao.class);
        traineeService = new TraineeService();
        traineeService.setTraineeDao(traineeDao);
    }

    @Test
    void testCreateTraineeGenerateUsernameAndPassword() {
        Trainee trainee = Trainee.builder()
                .firstName("Harry")
                .lastName("Potter")
                .build();

        when(traineeDao.findAll()).thenReturn(Collections.emptyList());

        traineeService.create(trainee);

        assertEquals("Harry.Potter", trainee.getUsername());
        assertNotNull(trainee.getPassword());
        assertEquals(10, trainee.getPassword().length());
        verify(traineeDao, times(1)).save(trainee);

    }


}