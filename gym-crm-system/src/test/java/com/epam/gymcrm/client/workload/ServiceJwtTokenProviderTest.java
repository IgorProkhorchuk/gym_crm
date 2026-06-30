package com.epam.gymcrm.client.workload;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.config.JwtProperties;
import com.epam.gymcrm.config.SecurityConfig;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

class ServiceJwtTokenProviderTest {

  private ServiceJwtTokenProvider serviceJwtTokenProvider;
  private JwtDecoder jwtDecoder;
  private Instant now;

  @BeforeEach
  void setUp() {
    JwtProperties jwtProperties =
        new JwtProperties(
            "https://gym-crm.test",
            "GymCrmTestJwtSecretKeyMustBeAtLeastThirtyTwoBytes",
            Duration.ofMinutes(30));
    SecurityConfig securityConfig = new SecurityConfig();
    JwtEncoder jwtEncoder = securityConfig.jwtEncoder(jwtProperties);
    jwtDecoder = securityConfig.jwtDecoder(jwtProperties);
    now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    serviceJwtTokenProvider = new ServiceJwtTokenProvider(jwtEncoder, jwtProperties, clock);
  }

  @Test
  void createServiceTokenShouldEncodeSignedJwtWithServiceRole() {
    String token = serviceJwtTokenProvider.createServiceToken();

    Jwt jwt = jwtDecoder.decode(token);

    assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
    assertThat(jwt.getIssuer().toString()).isEqualTo("https://gym-crm.test");
    assertThat(jwt.getSubject()).isEqualTo("gym-crm-system");
    assertThat(jwt.getIssuedAt()).isEqualTo(now);
    assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(Duration.ofMinutes(30)));
    assertThat(jwt.getId()).isNotBlank();
    List<String> roles = jwt.getClaimAsStringList("roles");
    assertThat(roles).containsExactly("SERVICE");
  }
}
