package com.epam.gymcrm.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.config.LoginAttemptProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisLoginAttemptServiceTest {

  private static final Duration LOCK_DURATION = Duration.ofMinutes(5);
  private static final String ATTEMPTS_KEY = "gym-crm:login-attempts:john.doe";
  private static final String BLOCKED_KEY = "gym-crm:login-blocked:john.doe";

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  private RedisLoginAttemptService loginAttemptService;

  @BeforeEach
  void setUp() {
    loginAttemptService =
        new RedisLoginAttemptService(redisTemplate, new LoginAttemptProperties(3, LOCK_DURATION));
  }

  @Test
  void isBlockedShouldReturnTrueWhenBlockedKeyExists() {
    when(redisTemplate.hasKey(BLOCKED_KEY)).thenReturn(true);

    boolean result = loginAttemptService.isBlocked(" John.Doe ");

    assertThat(result).isTrue();
  }

  @Test
  void isBlockedShouldReturnFalseWhenBlockedKeyDoesNotExist() {
    when(redisTemplate.hasKey(BLOCKED_KEY)).thenReturn(false);

    boolean result = loginAttemptService.isBlocked("John.Doe");

    assertThat(result).isFalse();
  }

  @Test
  void loginFailedShouldStoreAttemptWithTtlWhenLimitIsNotReached() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(1L);

    boolean result = loginAttemptService.loginFailed("John.Doe");

    assertThat(result).isFalse();
    verify(redisTemplate).expire(ATTEMPTS_KEY, LOCK_DURATION);
    verify(valueOperations, never()).set(BLOCKED_KEY, "true", LOCK_DURATION);
  }

  @Test
  void loginFailedShouldBlockUserAndClearAttemptsWhenLimitIsReached() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(3L);

    boolean result = loginAttemptService.loginFailed("John.Doe");

    assertThat(result).isTrue();
    verify(redisTemplate).expire(ATTEMPTS_KEY, LOCK_DURATION);
    verify(valueOperations).set(BLOCKED_KEY, "true", LOCK_DURATION);
    verify(redisTemplate).delete(ATTEMPTS_KEY);
  }

  @Test
  void loginSucceededShouldClearAttemptsAndBlockKeys() {
    loginAttemptService.loginSucceeded("John.Doe");

    verify(redisTemplate).delete(List.of(ATTEMPTS_KEY, BLOCKED_KEY));
  }
}
