package com.epam.gymcrm.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.web.auth.JwtRevocationService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidationException;

class SecurityConfigTest {

  private final SecurityConfig securityConfig = new SecurityConfig();
  private final JwtProperties jwtProperties =
      new JwtProperties(
          "https://gym-crm.test",
          "GymCrmTestJwtSecretKeyMustBeAtLeastThirtyTwoBytes",
          Duration.ofMinutes(30));

  @Test
  void jwtDecoderShouldAcceptJwtWhenTokenIdIsNotRevoked() {
    JwtRevocationService jwtRevocationService = mock(JwtRevocationService.class);
    when(jwtRevocationService.isRevoked("active-token")).thenReturn(false);

    Jwt jwt = jwtDecoder(jwtRevocationService).decode(jwtToken("active-token"));

    assertThat(jwt.getId()).isEqualTo("active-token");
  }

  @Test
  void jwtDecoderShouldRejectJwtWithoutTokenId() {
    JwtRevocationService jwtRevocationService = mock(JwtRevocationService.class);

    assertThatThrownBy(() -> jwtDecoder(jwtRevocationService).decode(jwtToken(null)))
        .isInstanceOf(JwtValidationException.class)
        .hasMessageContaining("JWT token id is missing");
  }

  @Test
  void jwtDecoderShouldRejectJwtWithBlankTokenId() {
    JwtRevocationService jwtRevocationService = mock(JwtRevocationService.class);

    assertThatThrownBy(() -> jwtDecoder(jwtRevocationService).decode(jwtToken(" ")))
        .isInstanceOf(JwtValidationException.class)
        .hasMessageContaining("JWT token id is missing");
  }

  @Test
  void jwtDecoderShouldRejectRevokedJwt() {
    JwtRevocationService jwtRevocationService = mock(JwtRevocationService.class);
    when(jwtRevocationService.isRevoked("revoked-token")).thenReturn(true);

    assertThatThrownBy(() -> jwtDecoder(jwtRevocationService).decode(jwtToken("revoked-token")))
        .isInstanceOf(JwtValidationException.class)
        .hasMessageContaining("JWT has been revoked");
  }

  private JwtDecoder jwtDecoder(JwtRevocationService jwtRevocationService) {
    return securityConfig.jwtDecoder(jwtProperties, jwtRevocationService);
  }

  private String jwtToken(String tokenId) {
    JwtEncoder jwtEncoder = securityConfig.jwtEncoder(jwtProperties);
    Instant now = Instant.now();
    JwtClaimsSet.Builder claims =
        JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .subject("John.Doe")
            .issuedAt(now)
            .expiresAt(now.plus(Duration.ofMinutes(5)))
            .claim("profileType", "TRAINEE")
            .claim("roles", List.of("TRAINEE"));
    if (tokenId != null) {
      claims.id(tokenId);
    }

    return jwtEncoder
        .encode(
            JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims.build()))
        .getTokenValue();
  }
}
