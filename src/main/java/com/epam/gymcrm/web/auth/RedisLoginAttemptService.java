package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.config.LoginAttemptProperties;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisLoginAttemptService implements LoginAttemptService {

  private static final String ATTEMPTS_KEY_PREFIX = "gym-crm:login-attempts:";
  private static final String BLOCKED_KEY_PREFIX = "gym-crm:login-blocked:";
  private static final String BLOCKED_VALUE = "true";

  private final StringRedisTemplate redisTemplate;
  private final LoginAttemptProperties properties;

  @Override
  public boolean isBlocked(String username) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(blockedKey(username)));
  }

  @Override
  public boolean loginFailed(String username) {
    String attemptsKey = attemptsKey(username);
    long attempts = redisTemplate.opsForValue().increment(attemptsKey);
    redisTemplate.expire(attemptsKey, properties.lockDuration());

    if (attempts >= properties.maxFailedAttempts()) {
      redisTemplate
          .opsForValue()
          .set(blockedKey(username), BLOCKED_VALUE, properties.lockDuration());
      redisTemplate.delete(attemptsKey);
      return true;
    }
    return false;
  }

  @Override
  public void loginSucceeded(String username) {
    redisTemplate.delete(List.of(attemptsKey(username), blockedKey(username)));
  }

  private static String attemptsKey(String username) {
    return ATTEMPTS_KEY_PREFIX + normalize(username);
  }

  private static String blockedKey(String username) {
    return BLOCKED_KEY_PREFIX + normalize(username);
  }

  private static String normalize(String username) {
    return username.strip().toLowerCase(Locale.ROOT);
  }
}
