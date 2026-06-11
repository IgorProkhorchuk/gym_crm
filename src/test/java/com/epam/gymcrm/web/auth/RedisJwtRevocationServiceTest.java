package com.epam.gymcrm.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class RedisJwtRevocationServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
  private static final String TOKEN_ID = "jwt-id";
  private static final String REVOKED_KEY = "gym-crm:revoked-jwt:" + TOKEN_ID;

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  private RedisJwtRevocationService jwtRevocationService;

  @BeforeEach
  void setUp() {
    jwtRevocationService =
        new RedisJwtRevocationService(redisTemplate, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void revokeShouldStoreJwtIdWithTimeToLiveWhenJwtIsNotExpired() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    jwtRevocationService.revoke(jwt(NOW.plus(Duration.ofMinutes(5))));

    verify(valueOperations).set(REVOKED_KEY, "true", Duration.ofMinutes(5));
  }

  @Test
  void revokeShouldNotStoreJwtIdWhenJwtIsExpired() {
    jwtRevocationService.revoke(jwt(NOW));

    verify(redisTemplate, never()).opsForValue();
    verifyNoInteractions(valueOperations);
  }

  @Test
  void isRevokedShouldReturnTrueWhenRevokedKeyExists() {
    when(redisTemplate.hasKey(REVOKED_KEY)).thenReturn(true);

    boolean result = jwtRevocationService.isRevoked(TOKEN_ID);

    assertThat(result).isTrue();
  }

  @Test
  void isRevokedShouldReturnFalseWhenRevokedKeyDoesNotExist() {
    when(redisTemplate.hasKey(REVOKED_KEY)).thenReturn(false);

    boolean result = jwtRevocationService.isRevoked(TOKEN_ID);

    assertThat(result).isFalse();
  }

  private static Jwt jwt(Instant expiresAt) {
    return Jwt.withTokenValue("token")
        .header("alg", "HS256")
        .subject("John.Doe")
        .issuedAt(NOW.minus(Duration.ofMinutes(10)))
        .expiresAt(expiresAt)
        .claim("jti", TOKEN_ID)
        .claim("roles", List.of("TRAINEE"))
        .build();
  }
}
