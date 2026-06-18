package com.epam.gymcrm.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.login-attempt")
public record LoginAttemptProperties(int maxFailedAttempts, Duration lockDuration) {

  public LoginAttemptProperties {
    if (maxFailedAttempts < 1) {
      throw new IllegalArgumentException("Max failed attempts must be positive");
    }
    if (lockDuration == null || lockDuration.isZero() || lockDuration.isNegative()) {
      throw new IllegalArgumentException("Lock duration must be positive");
    }
  }
}
