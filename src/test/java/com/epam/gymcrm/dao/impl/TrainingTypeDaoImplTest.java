package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.model.TrainingType;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;

class TrainingTypeDaoImplTest extends PostgresContainerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private TrainingTypeDao trainingTypeDao;

    @Test
    void findByNameShouldReturnTrainingTypeWhenNameExists() {
        TrainingType trainingType = trainingType("Fitness");
        entityManager.persist(trainingType);
        entityManager.flush();
        entityManager.clear();

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
