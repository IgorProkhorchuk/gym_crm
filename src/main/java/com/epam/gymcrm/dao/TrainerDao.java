package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;


import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for {@link Trainer} records keyed by {@link Trainer#getId()}.
 */
public interface TrainerDao {

    /**
     * Stores the trainer under its profile id, replacing any record with the same id.
     *
     * @param trainer trainer to insert or replace
     */
    void save(Trainer trainer);

    /**
     * Finds a trainer by profile id.
     *
     * @param id trainer profile id to look up
     * @return trainer with the given id, or {@link Optional#empty()} when absent
     */
    Optional<Trainer> findById(Long id);

    /**
     * Finds a trainer by the username stored in the associated user record.
     *
     * @param username trainer username to look up
     * @return trainer with the given username, or {@link Optional#empty()} when absent
     */
    Optional<Trainer> findByUsername(String username);

    /**
     * Removes a trainer by profile id.
     *
     * @param id trainer profile id to remove
     */
    void delete(Long id);

    /**
     * Returns a snapshot of all stored trainers.
     *
     * @return all trainers present when the method is called
     */
    List<Trainer> findAll();
}
