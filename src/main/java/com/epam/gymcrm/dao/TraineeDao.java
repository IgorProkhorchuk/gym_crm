package com.epam.gymcrm.dao;

import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for {@link Trainee} records keyed by {@link Trainee#getId()}.
 */
public interface TraineeDao {

    /**
     * Stores the trainee under its profile id, replacing any record with the same id.
     *
     * @param trainee trainee to insert or replace
     */
    void save(Trainee trainee);

    /**
     * Finds a trainee by profile id.
     *
     * @param id trainee profile id to look up
     * @return trainee with the given id, or {@link Optional#empty()} when absent
     */
    Optional<Trainee> findById(Long id);

    /**
     * Finds a trainee by the username stored in the associated user record.
     *
     * @param username trainee username to look up
     * @return trainee with the given username, or {@link Optional#empty()} when absent
     */
    Optional<Trainee> findByUsername(String username);

    /**
     * Removes a trainee by profile id.
     *
     * @param id trainee profile id to remove
     */
    void delete(Long id);

    /**
     * Returns a page of stored trainees.
     *
     * @param pageRequest pagination settings
     * @return trainees present on the requested page
     */
    List<Trainee> findAll(PageRequest pageRequest);
}
