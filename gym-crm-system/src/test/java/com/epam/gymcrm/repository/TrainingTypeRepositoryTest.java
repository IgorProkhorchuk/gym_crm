package com.epam.gymcrm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.model.TrainingType;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrainingTypeRepositoryTest extends PostgresContainerTest {

  @Resource private TrainingTypeRepository trainingTypeRepository;

  @Test
  void findByNameShouldReturnSeededTrainingTypeWhenNameExists() {
    Optional<TrainingType> found = trainingTypeRepository.findByName("Fitness");

    assertThat(found)
        .isPresent()
        .get()
        .extracting(TrainingType::getTrainingTypeName)
        .isEqualTo("Fitness");
  }

  @Test
  void findByNameShouldReturnEmptyOptionalWhenNameDoesNotExist() {
    Optional<TrainingType> found = trainingTypeRepository.findByName("Unknown");

    assertThat(found).isEmpty();
  }

  @Test
  void findAllByOrderByTrainingTypeIdAscShouldReturnSeededTrainingTypesOrderedById() {
    List<TrainingType> found = trainingTypeRepository.findAllByOrderByTrainingTypeIdAsc();

    assertThat(found)
        .extracting(TrainingType::getTrainingTypeName)
        .containsExactly("Fitness", "Yoga", "Zumba", "Stretching", "Resistance", "Cardio", "Boxing");
  }
}
