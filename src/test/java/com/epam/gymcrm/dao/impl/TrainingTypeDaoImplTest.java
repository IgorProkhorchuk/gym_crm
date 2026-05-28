package com.epam.gymcrm.dao.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.model.TrainingType;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrainingTypeDaoImplTest extends PostgresContainerTest {

  @Resource private TrainingTypeDao trainingTypeDao;

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

  @Test
  void findAllShouldReturnSeededTrainingTypesOrderedById() {
    List<TrainingType> found = trainingTypeDao.findAll();

    assertThat(found)
        .extracting(TrainingType::getTrainingTypeName)
        .containsExactly("Fitness", "Yoga", "Zumba", "Stretching", "Resistance", "Cardio", "Boxing");
  }
}
