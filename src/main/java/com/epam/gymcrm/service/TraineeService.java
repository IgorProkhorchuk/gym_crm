package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;

import java.util.List;

/**
 * Business operations for trainee profiles.
 */
public interface TraineeService {

    /**
     * Creates a trainee profile after generating credentials.
     * The provided trainee is mutated with a generated username and password before it is saved.
     *
     * @param trainee trainee profile data; first name and last name are used to generate the username
     */
    void create(Trainee trainee);

    /**
     * Returns a trainee profile after authenticating the trainee credentials.
     *
     * @param username trainee username
     * @param password trainee password
     * @return authenticated trainee profile
     */
    Trainee getProfile(String username, String password);

    /**
     * Changes a trainee password after authenticating the current credentials.
     *
     * @param username trainee username
     * @param oldPassword current trainee password
     * @param newPassword replacement trainee password
     */
    void changePassword(String username, String oldPassword, String newPassword);

    /**
     * Activates a trainee profile after authenticating the trainee credentials.
     * This operation is not idempotent and fails when the profile is already active.
     *
     * @param username trainee username
     * @param password trainee password
     */
    void activate(String username, String password);

    /**
     * Deactivates a trainee profile after authenticating the trainee credentials.
     * This operation is not idempotent and fails when the profile is already inactive.
     *
     * @param username trainee username
     * @param password trainee password
     */
    void deactivate(String username, String password);

    /**
     * Hard deletes a trainee profile by username after authenticating the trainee credentials.
     * Relevant trainings are deleted by cascade.
     *
     * @param username trainee username
     * @param password trainee password
     */
    void deleteByUsername(String username, String password);

    /**
     * Replaces an authenticated trainee's assigned trainers with the provided trainer usernames.
     *
     * @param username trainee username
     * @param password trainee password
     * @param trainerUsernames usernames of trainers to assign
     * @return updated assigned trainers list
     */
    List<Trainer> updateTrainers(String username, String password, List<String> trainerUsernames);

    /**
     * Saves trainee profile changes, replacing the stored record with the same user id.
     *
     * @param trainee trainee data to save
     * @throws EntityNotFoundException when no trainee exists with the given id
     */
    void update(Trainee trainee);

    /**
     * Deletes a trainee profile by user id.
     *
     * @param id user id to remove
     */
    void delete(Long id);

    /**
     * Finds a trainee profile by user id.
     *
     * @param id user id to look up
     * @return trainee with the given id
     * @throws EntityNotFoundException when no trainee exists with the given id
     */
    Trainee findById(Long id);
}
