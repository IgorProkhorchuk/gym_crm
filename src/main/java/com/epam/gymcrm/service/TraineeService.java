package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;

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
     * Saves trainee profile changes, replacing the stored record with the same user id.
     *
     * @param trainee trainee data to save
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
