package com.epam.gymcrm.web.auth;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisJwtRevocationService implements JwtRevocationService {

  private static final String REVOKED_TOKEN_KEY_PREFIX = "gym-crm:revoked-jwt:";
  private static final String REVOKED_VALUE = "true";

  private final StringRedisTemplate redisTemplate;
  private final Clock clock;

  @Override
  public void revoke(Jwt jwt) {
    String tokenId = Objects.requireNonNull(jwt.getId(), "JWT id must not be null");
    Duration timeToLive =
        Duration.between(
            clock.instant(), Objects.requireNonNull(jwt.getExpiresAt(), "JWT expiration missing"));

    if (timeToLive.compareTo(Duration.ZERO) > 0) {
      redisTemplate.opsForValue().set(revokedTokenKey(tokenId), REVOKED_VALUE, timeToLive);
    }
  }

  @Override
  public boolean isRevoked(String tokenId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(revokedTokenKey(tokenId)));
  }

  private static String revokedTokenKey(String tokenId) {
    return REVOKED_TOKEN_KEY_PREFIX + tokenId;
  }
}
