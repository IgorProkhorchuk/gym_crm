package com.epam.gymcrm.web.auth;

import org.springframework.security.oauth2.jwt.Jwt;

/** Stores and checks revoked JWT identifiers. */
public interface JwtRevocationService {

  /** Revokes the JWT until its expiration time. */
  void revoke(Jwt jwt);

  /** Checks whether a JWT identifier was revoked. */
  boolean isRevoked(String tokenId);
}
