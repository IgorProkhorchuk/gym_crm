package com.epam.gymcrm.dao;

import com.epam.gymcrm.dto.PageRequest;
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
     * Finds active trainers that are not assigned to the trainee with the given username.
     *
     * @param traineeUsername trainee username to check assignments for
     * @return active trainers not assigned to the trainee, or an empty list when the trainee does not exist
     */
    List<Trainer> findNotAssignedToTrainee(String traineeUsername);

    /**
     * Removes a trainer by profile id.
     *
     * @param id trainer profile id to remove
     */
    void delete(Long id);

    /**
     * Returns a page of stored trainers.
     *
     * @param pageRequest pagination settings
     * @return trainers present on the requested page
     */
    List<Trainer> findAll(PageRequest pageRequest);
}
