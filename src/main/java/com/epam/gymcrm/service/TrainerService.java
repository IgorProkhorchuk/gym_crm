package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import java.util.List;

/** Business operations for trainer profiles. */
public interface TrainerService {

  /**
   * Creates a trainer profile after generating credentials.
   *
   * @param request trainer profile data; first name and last name are used to generate the username
   * @return generated trainer credentials
   */
  UsernamePasswordResponse create(CreateTrainerRequest request);

  /**
   * Returns a trainer profile after authenticating the trainer credentials.
   *
   * @param request trainer credentials
   * @return authenticated trainer profile
   */
  TrainerProfileResponse getProfile(AuthRequest request);

  /**
   * Changes a trainer password after authenticating the current credentials.
   *
   * @param request password change data
   */
  void changePassword(ChangePasswordRequest request);

  /**
   * Switches a trainer profile active status after authenticating the trainer credentials. This
   * operation is not idempotent: each successful call flips the current status.
   *
   * @param request trainer credentials
   */
  void switchActiveStatus(AuthRequest request);

  /**
   * Returns active trainers that are not assigned to an authenticated trainee profile.
   *
   * @param request trainee credentials
   * @return active trainers not assigned to the trainee
   */
  List<TrainerSummaryResponse> getUnassignedTrainers(AuthRequest request);

  /**
   * Saves trainer profile changes after authenticating the trainer credentials.
   *
   * @param request trainer update data
   * @return updated trainer profile
   * @throws com.epam.gymcrm.exception.AuthenticationException when credentials are invalid or the
   *     payload does not belong to the authenticated trainer
   */
  TrainerProfileResponse update(UpdateTrainerRequest request);
}
