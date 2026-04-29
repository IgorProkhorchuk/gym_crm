package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingDaoImplTest {

    @InjectMocks
    private TrainingDaoImpl trainingDao;

    @Mock
    private InMemoryStorage storage;

    private Map<Long, Training> trainingMap;

    @BeforeEach
    void setUp() {
        trainingMap = new HashMap<>();
        when(storage.getStorage(Training.class)).thenReturn(trainingMap);
    }

    @Test
    void saveShouldStoreTrainingAndFindByIdShouldReturnIt() {
        Training training = Training.builder()
                .trainingId(1L)
                .trainingName("Yoga Basics")
                .build();

        trainingDao.save(training);

        Optional<Training> found = trainingDao.findById(1L);
        assertThat(found)
                .isPresent()
                .get()
                .extracting(Training::getTrainingName)
                .isEqualTo("Yoga Basics");
    }

    @Test
    void findByIdShouldReturnEmptyOptionalWhenTrainingDoesNotExist() {
        Optional<Training> found = trainingDao.findById(99L);
        assertThat(found).isEmpty();
    }
}
