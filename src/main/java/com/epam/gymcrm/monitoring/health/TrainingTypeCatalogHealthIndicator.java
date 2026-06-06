package com.epam.gymcrm.monitoring.health;

import com.epam.gymcrm.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainingTypeCatalogHealthIndicator implements HealthIndicator {

  private final TrainingTypeRepository trainingTypeRepository;

  @Override
  public Health health() {
    try {
      long trainingTypeCount = trainingTypeRepository.count();
      Health.Builder health = trainingTypeCount > 0 ? Health.up() : Health.down();
      return health.withDetail("trainingTypeCount", trainingTypeCount).build();
    } catch (RuntimeException exception) {
      return Health.down(exception).build();
    }
  }
}
