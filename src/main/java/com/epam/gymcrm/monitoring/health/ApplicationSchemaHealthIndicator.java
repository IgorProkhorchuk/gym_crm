package com.epam.gymcrm.monitoring.health;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationSchemaHealthIndicator implements HealthIndicator {

  private static final List<String> CORE_TABLES =
      List.of("users", "trainees", "trainers", "training_types", "trainings");

  private final JdbcTemplate jdbcTemplate;

  @Override
  public Health health() {
    try {
      CORE_TABLES.forEach(table -> jdbcTemplate.execute("select 1 from " + table + " where 1 = 0"));
      return Health.up().withDetail("checkedTables", CORE_TABLES).build();
    } catch (RuntimeException exception) {
      return Health.down(exception).withDetail("checkedTables", CORE_TABLES).build();
    }
  }
}
