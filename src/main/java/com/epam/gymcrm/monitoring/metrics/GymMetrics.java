package com.epam.gymcrm.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

  private static final String LOGIN_FAILED_METRIC = "gym.auth.login.failed";
  private static final String REASON_TAG = "reason";
  private static final String INVALID_CREDENTIALS = "invalid_credentials";

  private final Counter loginFailedInvalidCredentials;

  public GymMetrics(MeterRegistry meterRegistry) {
    this.loginFailedInvalidCredentials =
        Counter.builder(LOGIN_FAILED_METRIC)
            .tag(REASON_TAG, INVALID_CREDENTIALS)
            .register(meterRegistry);
  }

  public void recordLoginFailedInvalidCredentials() {
    loginFailedInvalidCredentials.increment();
  }
}
