package com.epam.gymcrm.web.auth;

/** Manages authentication tokens for REST requests. */
public interface TokenService {

  /** Creates a new token for the authenticated user. */
  String createToken(AuthenticatedUser user);

  /** Returns the authenticated user associated with the token. */
  AuthenticatedUser getUserByToken(String token);

  /** Updates stored credentials associated with the token. */
  void updatePassword(String token, String newPassword);
}
