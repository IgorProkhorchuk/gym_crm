package com.epam.gymcrm.client.workload;

import com.epam.gymcrm.config.JwtProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Creates JWT tokens for internal service-to-service requests.
 */
@Component
@RequiredArgsConstructor
public class ServiceJwtTokenProvider {

  private static final String SERVICE_SUBJECT = "gym-crm-system";
  private static final String SERVICE_ROLE = "SERVICE";

  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;
  private final Clock clock;

  /**
   * Creates a signed JWT for internal trainer workload calls.
   *
   * @return signed JWT token value
   */
  public String createServiceToken() {
    Instant issuedAt = clock.instant();
    Instant expiresAt = issuedAt.plus(jwtProperties.tokenLifetime());

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .issuer(jwtProperties.issuer())
            .subject(SERVICE_SUBJECT)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim("roles", List.of(SERVICE_ROLE))
            .build();

    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }
}
