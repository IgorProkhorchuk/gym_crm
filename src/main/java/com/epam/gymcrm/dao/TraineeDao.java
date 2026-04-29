package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for {@link Trainee} records keyed by {@link Trainee#getUserId()}.
 */
public interface TraineeDao {

    /**
     * Stores the trainee under its user id, replacing any record with the same id.
     *
     * @param trainee trainee to insert or replace
     */
    void save(Trainee trainee);

    /**
     * Finds a trainee by user id.
     *
     * @param id user id to look up
     * @return trainee with the given id, or {@link Optional#empty()} when absent
     */
    Optional<Trainee> findById(Long id);

    /**
     * Removes a trainee by user id.
     *
     * @param id user id to remove
     */
    void delete(Long id);

    /**
     * Returns a snapshot of all stored trainees.
     *
     * @return all trainees present when the method is called
     */
    List<Trainee> findAll();
}
