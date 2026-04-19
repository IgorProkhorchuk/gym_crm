package com.epam.gymcrm.service;


import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

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

    @Test
    void testGenerateUsernameFillsTheGap() {
        Trainee newTrainee = Trainee.builder().firstName("John").lastName("Doe").build();

        // Імітуємо ситуацію: є базовий юзер і юзер з цифрою 2, але цифра 1 - вільна!
        Trainee existingBase = Trainee.builder().username("John.Doe").build();
        Trainee existing2 = Trainee.builder().username("John.Doe2").build();
        Trainee similarName = Trainee.builder().username("John.Doering").build(); // Той самий Edge Case!

        when(traineeDao.findAll()).thenReturn(List.of(existingBase, existing2, similarName));

        traineeService.create(newTrainee);

        // Очікуємо, що сервіс проігнорує "John.Doering", побачить, що "John.Doe" та "John.Doe2" зайняті,
        // і видасть саме "John.Doe1"
        assertEquals("John.Doe1", newTrainee.getUsername());
    }

}