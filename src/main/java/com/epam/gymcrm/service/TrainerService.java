package com.epam.gymcrm.service;

import com.epam.gymcrm.model.Trainer;
import java.util.Optional;

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
     * Saves trainer profile changes, replacing the stored record with the same user id.
     *
     * @param trainer trainer data to save
     */
    void update(Trainer trainer);

    /**
     * Finds a trainer profile by user id.
     *
     * @param id user id to look up
     * @return trainer with the given id, or {@link Optional#empty()} when absent
     */
    Optional<Trainer> findById(Long id);
}
