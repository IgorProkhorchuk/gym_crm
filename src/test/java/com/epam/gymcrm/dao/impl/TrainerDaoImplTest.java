package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.model.Trainer;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerDaoImplTest {

    @InjectMocks
    private TrainerDaoImpl trainerDao;

    @Mock
    private InMemoryStorage storage;

    private Map<Long, Trainer> trainerMap;

    @BeforeEach
    void setUp() {
        trainerMap = new HashMap<>();
        when(storage.getStorage(Trainer.class)).thenReturn(trainerMap);
    }

    @Test
    void saveShouldStoreTrainerAndFindByIdShouldReturnIt() {
        Trainer trainer = Trainer.builder()
                .userId(1L)
                .firstName("Alice")
                .specialization("Fitness")
                .build();

        trainerDao.save(trainer);

        Optional<Trainer> found = trainerDao.findById(1L);
        assertAll(
                () -> assertThat(found)
                        .isPresent()
                        .get()
                        .extracting(Trainer::getFirstName)
                        .isEqualTo("Alice"),
                () -> assertThat(found)
                        .isPresent()
                        .get()
                        .extracting(Trainer::getSpecialization)
                        .isEqualTo("Fitness")
        );
    }

    @Test
    void findByIdShouldReturnEmptyOptionalWhenTrainerDoesNotExist() {
        Optional<Trainer> found = trainerDao.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void deleteShouldRemoveTrainerById() {
        Trainer trainer = Trainer.builder().userId(2L).firstName("Bob").build();
        trainerDao.save(trainer);

        trainerDao.delete(2L);

        Optional<Trainer> found = trainerDao.findById(2L);
        assertThat(found).as("Trainer should be removed from storage").isEmpty();
    }

    @Test
    void findAllShouldReturnAllTrainers() {
        Trainer t1 = Trainer.builder().userId(10L).firstName("Trainer1").build();
        Trainer t2 = Trainer.builder().userId(20L).firstName("Trainer2").build();

        trainerDao.save(t1);
        trainerDao.save(t2);

        List<Trainer> all = trainerDao.findAll();

        assertThat(all).containsExactlyInAnyOrder(t1, t2);
    }
}
