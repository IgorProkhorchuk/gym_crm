package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainer;

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
     * Saves trainer profile changes, replacing the stored record with the same trainer profile id.
     *
     * @param trainer trainer data to save
     */
    void update(Trainer trainer);

    /**
     * Finds a trainer profile by trainer profile id.
     *
     * @param id trainer profile id to look up
     * @return trainer with the given id
     * @throws EntityNotFoundException when no trainer exists with the given id
     */
    Trainer findById(Long id);
}
