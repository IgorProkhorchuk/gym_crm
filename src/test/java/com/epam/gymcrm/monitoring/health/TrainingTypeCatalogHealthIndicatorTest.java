package com.epam.gymcrm.monitoring.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

@ExtendWith(MockitoExtension.class)
class TrainingTypeCatalogHealthIndicatorTest {

  @Mock private TrainingTypeRepository trainingTypeRepository;

  @Test
  void healthShouldBeUpWhenTrainingTypeCatalogIsNotEmpty() {
    when(trainingTypeRepository.count()).thenReturn(7L);

    Health health = indicator().health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsEntry("trainingTypeCount", 7L);
  }

  @Test
  void healthShouldBeDownWhenTrainingTypeCatalogIsEmpty() {
    when(trainingTypeRepository.count()).thenReturn(0L);

    Health health = indicator().health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsEntry("trainingTypeCount", 0L);
  }

  @Test
  void healthShouldBeDownWhenRepositoryFails() {
    when(trainingTypeRepository.count()).thenThrow(new RuntimeException("catalog unavailable"));

    Health health = indicator().health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsKey("error");
  }

  private TrainingTypeCatalogHealthIndicator indicator() {
    return new TrainingTypeCatalogHealthIndicator(trainingTypeRepository);
  }
}
