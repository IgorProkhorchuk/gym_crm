package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.model.TrainingType;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingTypeDaoImplTest extends PostgresContainerTest {

    @Resource
    private TrainingTypeDao trainingTypeDao;

    @Test
    void findByNameShouldReturnSeededTrainingTypeWhenNameExists() {
        Optional<TrainingType> found = trainingTypeDao.findByName("Fitness");

        assertThat(found)
                .isPresent()
                .get()
                .extracting(TrainingType::getTrainingTypeName)
                .isEqualTo("Fitness");
    }

    @Test
    void findByNameShouldReturnEmptyOptionalWhenNameDoesNotExist() {
        Optional<TrainingType> found = trainingTypeDao.findByName("Unknown");

        assertThat(found).isEmpty();
    }
}
