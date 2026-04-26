package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoImplTest {

    @InjectMocks
    private TraineeDaoImpl traineeDao;

    @Mock
    private InMemoryStorage storage;

    private Map<Long, Trainee> traineeMap;

    @BeforeEach
    void setUp() {
        traineeMap = new HashMap<>();
        when(storage.getStorage(Trainee.class)).thenReturn(traineeMap);
    }

    @Test
    void testSaveAndFindById() {
        Trainee trainee = Trainee.builder().userId(1L).firstName("Oleg").build();

        traineeDao.save(trainee);

        Optional<Trainee> found = traineeDao.findById(1L);
        assertAll(
                () -> assertThat(found)
                        .isPresent()
                        .get()
                        .extracting(Trainee::getFirstName)
                        .isEqualTo("Oleg"),
                () -> verify(storage, atLeastOnce()).getStorage(Trainee.class)
        );
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Trainee> found = traineeDao.findById(99L);
        assertThat(found).isEmpty();
    }

    @Test
    void testDelete() {
        Trainee trainee = Trainee.builder().userId(2L).firstName("Ivan").build();
        traineeDao.save(trainee);

        traineeDao.delete(2L);

        Optional<Trainee> found = traineeDao.findById(2L);
        assertThat(found).as("Trainee should be deleted").isEmpty();
    }

    @Test
    void testFindAll() {
        Trainee trainee1 = Trainee.builder().userId(3L).firstName("Anna").build();
        Trainee trainee2 = Trainee.builder().userId(4L).firstName("Maria").build();

        traineeDao.save(trainee1);
        traineeDao.save(trainee2);

        List<Trainee> all = traineeDao.findAll();

        assertThat(all).containsExactlyInAnyOrder(trainee1, trainee2);
    }
}
