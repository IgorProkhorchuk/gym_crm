package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Training;

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
     * @return training session with the given id
     * @throws EntityNotFoundException when no training session exists with the given id
     */
    Training findById(Long id);
}
