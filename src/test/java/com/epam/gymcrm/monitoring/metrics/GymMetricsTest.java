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
}
