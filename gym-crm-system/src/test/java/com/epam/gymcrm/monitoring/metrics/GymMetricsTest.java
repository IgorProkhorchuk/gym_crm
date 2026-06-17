package com.epam.gymcrm.monitoring.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class GymMetricsTest {

  private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
  private final GymMetrics gymMetrics = new GymMetrics(meterRegistry);

  @Test
  void recordLoginFailedInvalidCredentialsShouldIncrementTaggedLoginFailureCounter() {
    gymMetrics.recordLoginFailedInvalidCredentials();

    assertThat(
            meterRegistry
                .counter("gym.auth.login.failed", "reason", "invalid_credentials")
                .count())
        .isEqualTo(1.0);
  }

  @Test
  void recordTrainingCreationSucceededShouldIncrementSuccessCounter() {
    gymMetrics.recordTrainingCreationSucceeded();

    assertThat(meterRegistry.counter("gym.training.creation.succeeded").count()).isEqualTo(1.0);
  }

  @Test
  void recordTrainingCreationFailuresShouldIncrementTaggedFailureCounters() {
    gymMetrics.recordTrainingCreationAuthFailed();
    gymMetrics.recordTrainingCreationTrainerNotFound();
    gymMetrics.recordTrainingCreationTrainingTypeNotFound();

    assertThat(
            meterRegistry
                .counter("gym.training.creation.failed", "reason", "auth_failed")
                .count())
        .isEqualTo(1.0);
    assertThat(
            meterRegistry
                .counter("gym.training.creation.failed", "reason", "trainer_not_found")
                .count())
        .isEqualTo(1.0);
    assertThat(
            meterRegistry
                .counter("gym.training.creation.failed", "reason", "training_type_not_found")
                .count())
        .isEqualTo(1.0);
  }
}
