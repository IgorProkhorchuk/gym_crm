package com.epam.gymcrm.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class LoginAttemptPropertiesTest {

  @Test
  void constructorShouldAcceptPositiveValues() {
    assertThatCode(() -> new LoginAttemptProperties(3, Duration.ofMinutes(5)))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void constructorShouldRejectNonPositiveMaxFailedAttempts(int maxFailedAttempts) {
    assertThatThrownBy(
            () -> new LoginAttemptProperties(maxFailedAttempts, Duration.ofMinutes(5)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Max failed attempts must be positive");
  }

  @ParameterizedTest
  @MethodSource("invalidLockDurations")
  void constructorShouldRejectNonPositiveLockDuration(Duration lockDuration) {
    assertThatThrownBy(() -> new LoginAttemptProperties(3, lockDuration))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Lock duration must be positive");
  }

  private static Stream<Duration> invalidLockDurations() {
    return Stream.<Duration>of(null, Duration.ZERO, Duration.ofSeconds(-1));
  }
}
