package com.epam.gymcrm.monitoring.health;

import com.epam.gymcrm.dao.TrainingTypeDao;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainingTypeCatalogHealthIndicator implements HealthIndicator {

  private final TrainingTypeDao trainingTypeDao;

  @Override
  public Health health() {
    try {
      long trainingTypeCount = trainingTypeDao.count();
      Health.Builder health = trainingTypeCount > 0 ? Health.up() : Health.down();
      return health.withDetail("trainingTypeCount", trainingTypeCount).build();
    } catch (RuntimeException exception) {
      return Health.down(exception).build();
    }
  }
}
