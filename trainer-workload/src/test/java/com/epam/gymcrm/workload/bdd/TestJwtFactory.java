package com.epam.gymcrm.workload.bdd;

import com.epam.gymcrm.workload.config.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

public class TestJwtFactory {

  private final JwtProperties jwtProperties;

  public TestJwtFactory(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
  }

  public String serviceToken() {
    return tokenWithRole("SERVICE");
  }

  public String tokenWithRole(String role) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .subject("gym-crm-system")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .claim("roles", List.of(role))
            .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return encoder().encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  private NimbusJwtEncoder encoder() {
    SecretKey key =
        new SecretKeySpec(
            jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
            "HmacSHA256");
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }
}
