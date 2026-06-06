package com.epam.gymcrm.monitoring.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class ApplicationSchemaHealthIndicatorTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @Test
  void healthShouldBeUpWhenCoreTablesAreAccessible() {
    Health health = indicator().health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsKey("checkedTables");
    verify(jdbcTemplate).execute("select 1 from users where 1 = 0");
    verify(jdbcTemplate).execute("select 1 from trainees where 1 = 0");
    verify(jdbcTemplate).execute("select 1 from trainers where 1 = 0");
    verify(jdbcTemplate).execute("select 1 from training_types where 1 = 0");
    verify(jdbcTemplate).execute("select 1 from trainings where 1 = 0");
  }

  @Test
  void healthShouldBeDownWhenCoreTableIsNotAccessible() {
    doThrow(new RuntimeException("schema check failed"))
        .when(jdbcTemplate)
        .execute("select 1 from users where 1 = 0");

    Health health = indicator().health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsKeys("checkedTables", "error");
  }

  @Test
  void constructorShouldRejectInvalidTableNames() {
    assertThatThrownBy(
            () ->
                new ApplicationSchemaHealthIndicator(
                    jdbcTemplate, List.of("users", "trainings; drop table users")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid application schema health table");
  }

  private ApplicationSchemaHealthIndicator indicator() {
    return new ApplicationSchemaHealthIndicator(
        jdbcTemplate, List.of("users", "trainees", "trainers", "training_types", "trainings"));
  }
}
