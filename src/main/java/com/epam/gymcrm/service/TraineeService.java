package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;

import java.util.List;

/**
 * Business operations for trainee profiles.
 */
public interface TraineeService {

    /**
     * Creates a trainee profile after generating credentials.
     *
     * @param request trainee profile data; first name and last name are used to generate the username
     * @return generated trainee credentials
     */
    UsernamePasswordResponse create(CreateTraineeRequest request);

    /**
     * Returns a trainee profile after authenticating the trainee credentials.
     *
     * @param request trainee credentials
     * @return authenticated trainee profile
     */
    TraineeProfileResponse getProfile(AuthRequest request);

    /**
     * Changes a trainee password after authenticating the current credentials.
     *
     * @param request password change data
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Switches a trainee profile active status after authenticating the trainee credentials.
     * This operation is not idempotent: each successful call flips the current status.
     *
     * @param request trainee credentials
     */
    void switchActiveStatus(AuthRequest request);

    /**
     * Hard deletes a trainee profile by username after authenticating the trainee credentials.
     * Relevant trainings are deleted by cascade.
     *
     * @param request trainee credentials
     */
    void deleteByUsername(AuthRequest request);

    /**
     * Replaces an authenticated trainee's assigned trainers with the provided trainer usernames.
     *
     * @param request trainee credentials and trainer usernames to assign
     * @return updated assigned trainers list
     */
    List<TrainerSummaryResponse> updateTrainers(UpdateTraineeTrainersRequest request);

    /**
     * Saves trainee profile changes after authenticating the trainee credentials.
     *
     * @param request trainee update data
     * @return updated trainee profile
     * @throws com.epam.gymcrm.exception.AuthenticationException when credentials are invalid
     *         or the payload does not belong to the authenticated trainee
     */
    TraineeProfileResponse update(UpdateTraineeRequest request);

}
