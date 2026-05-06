package com.epam.gymcrm.dao;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.model.Training;

import java.util.List;
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

    /**
     * Finds trainee trainings by trainee username and optional criteria.
     *
     * @param traineeUsername trainee username to search trainings for
     * @param criteria optional filters
     * @return trainings matching the given username and filters
     */
    List<Training> findByTraineeUsernameAndCriteria(String traineeUsername, TraineeTrainingCriteria criteria);

    /**
     * Finds trainer trainings by trainer username and optional criteria.
     *
     * @param trainerUsername trainer username to search trainings for
     * @param criteria optional filters
     * @return trainings matching the given username and filters
     */
    List<Training> findByTrainerUsernameAndCriteria(String trainerUsername, TrainerTrainingCriteria criteria);
}
