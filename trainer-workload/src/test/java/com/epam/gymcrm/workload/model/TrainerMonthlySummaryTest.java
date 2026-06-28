package com.epam.gymcrm.workload.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import jakarta.persistence.Version;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class TrainerMonthlySummaryTest {

  @Test
  void versionShouldBeMappedForOptimisticLocking() throws NoSuchFieldException {
    Field versionField = TrainerMonthlySummary.class.getDeclaredField("version");
    Column column = versionField.getAnnotation(Column.class);

    assertThat(versionField.getAnnotation(Version.class)).isNotNull();
    assertThat(column).isNotNull();
    assertThat(column.name()).isEqualTo("version");
    assertThat(column.nullable()).isFalse();
  }
}
