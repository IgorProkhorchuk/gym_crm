package com.epam.gymcrm.service;

import com.epam.gymcrm.model.Training;
import java.util.Optional;

/**
 * Business operations for training sessions.
 */
public interface TrainingService {

    /**
     * Saves a training session, replacing any stored session with the same training id.
     *
     * @param training training session to save
     */
    void create(Training training);

    /**
     * Finds a training session by training id.
     *
     * @param id training id to look up
     * @return training session with the given id, or {@link Optional#empty()} when absent
     */
    Optional<Training> findById(Long id);
}
