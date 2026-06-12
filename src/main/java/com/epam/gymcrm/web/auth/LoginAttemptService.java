package com.epam.gymcrm.web.auth;

/** Tracks unsuccessful login attempts and temporary account blocks. */
public interface LoginAttemptService {

  /** Checks whether login is temporarily blocked for the username. */
  boolean isBlocked(String username);

  /** Records an unsuccessful login attempt and returns whether the user is now blocked. */
  boolean loginFailed(String username);

  /** Clears unsuccessful login attempts and temporary block state after a successful login. */
  void loginSucceeded(String username);
}
