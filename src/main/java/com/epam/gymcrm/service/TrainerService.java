package com.epam.gymcrm.service;

import com.epam.gymcrm.model.Trainer;

import java.util.List;

/**
 * Business operations for trainer profiles.
 */
public interface TrainerService {

    /**
     * Creates a trainer profile after generating credentials.
     * The provided trainer is mutated with a generated username and password before it is saved.
     *
     * @param trainer trainer profile data; first name and last name are used to generate the username
     */
    void create(Trainer trainer);

    /**
     * Returns a trainer profile after authenticating the trainer credentials.
     *
     * @param username trainer username
     * @param password trainer password
     * @return authenticated trainer profile
     */
    Trainer getProfile(String username, String password);

    /**
     * Changes a trainer password after authenticating the current credentials.
     *
     * @param username trainer username
     * @param oldPassword current trainer password
     * @param newPassword replacement trainer password
     */
    void changePassword(String username, String oldPassword, String newPassword);

    /**
     * Activates a trainer profile after authenticating the trainer credentials.
     * This operation is not idempotent and fails when the profile is already active.
     *
     * @param username trainer username
     * @param password trainer password
     */
    void activate(String username, String password);

    /**
     * Deactivates a trainer profile after authenticating the trainer credentials.
     * This operation is not idempotent and fails when the profile is already inactive.
     *
     * @param username trainer username
     * @param password trainer password
     */
    void deactivate(String username, String password);

    /**
     * Returns active trainers that are not assigned to an authenticated trainee profile.
     *
     * @param traineeUsername trainee username
     * @param traineePassword trainee password
     * @return active trainers not assigned to the trainee
     */
    List<Trainer> getUnassignedTrainers(String traineeUsername, String traineePassword);

    /**
     * Saves trainer profile changes after authenticating the trainer credentials.
     *
     * @param username trainer username
     * @param password trainer password
     * @param trainer trainer data to save
     * @throws com.epam.gymcrm.exception.AuthenticationException when credentials are invalid
     *         or the payload does not belong to the authenticated trainer
     */
    void update(String username, String password, Trainer trainer);

}
