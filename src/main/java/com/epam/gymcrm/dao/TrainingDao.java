package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Training;

import java.util.Optional;

/**
 * Persistence contract for {@link Training} records keyed by {@link Training#getTrainingId()}.
 */
public interface TrainingDao {

    /**
     * Stores the training under its training id, replacing any record with the same id.
     *
     * @param training training to insert or replace
     */
    void save(Training training);

    /**
     * Finds a training by training id.
     *
     * @param id training id to look up
     * @return training with the given id, or {@link Optional#empty()} when absent
     */
    Optional<Training> findById(Long id);
}
