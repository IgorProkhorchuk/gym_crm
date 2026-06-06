package com.epam.gymcrm.monitoring.health;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApplicationSchemaHealthIndicator implements HealthIndicator {

  private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

  private final JdbcTemplate jdbcTemplate;
  private final List<String> coreTables;

  public ApplicationSchemaHealthIndicator(
      JdbcTemplate jdbcTemplate,
      @Value("${management.health.schema.core-tables}") List<String> coreTables) {
    this.jdbcTemplate = jdbcTemplate;
    this.coreTables = validateCoreTables(coreTables);
  }

  @Override
  public Health health() {
    try {
      coreTables.forEach(table -> jdbcTemplate.execute("select 1 from " + table + " where 1 = 0"));
      return Health.up().withDetail("checkedTables", coreTables).build();
    } catch (RuntimeException exception) {
      return Health.down(exception).withDetail("checkedTables", coreTables).build();
    }
  }

  private static List<String> validateCoreTables(List<String> coreTables) {
    if (coreTables == null || coreTables.isEmpty()) {
      throw new IllegalArgumentException("Application schema health tables must not be empty");
    }

    return coreTables.stream()
        .map(String::trim)
        .peek(ApplicationSchemaHealthIndicator::validateTableName)
        .toList();
  }

  private static void validateTableName(String tableName) {
    if (!TABLE_NAME_PATTERN.matcher(tableName).matches()) {
      throw new IllegalArgumentException("Invalid application schema health table: " + tableName);
    }
  }
}
