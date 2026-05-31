package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;

/** Verifies profile credentials before protected business operations are executed. */
public interface AuthenticationService {

  /**
   * Authenticates a trainee by username and password.
   *
   * @param username trainee username
   * @param password trainee password
   * @return authenticated trainee profile
   * @throws AuthenticationException when username or password does not match a trainee profile
   */
  Trainee authenticateTrainee(String username, String password);

  /**
   * Authenticates a trainer by username and password.
   *
   * @param username trainer username
   * @param password trainer password
   * @return authenticated trainer profile
   * @throws AuthenticationException when username or password does not match a trainer profile
   */
  Trainer authenticateTrainer(String username, String password);
}
