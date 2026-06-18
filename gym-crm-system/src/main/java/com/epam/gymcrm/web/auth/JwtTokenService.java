package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.config.JwtProperties;
import com.epam.gymcrm.dto.auth.ProfileType;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;
  private final Clock clock;

  public String createToken(String username, ProfileType profileType) {
    Instant issuedAt = clock.instant();
    Instant expiresAt = issuedAt.plus(jwtProperties.tokenLifetime());

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .issuer(jwtProperties.issuer())
            .subject(username)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim("profileType", profileType.name())
            .claim("roles", List.of(profileType.name()))
            .build();

    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }
}
