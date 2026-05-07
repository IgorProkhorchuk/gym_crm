package com.epam.gymcrm.service;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.model.Training;

import java.util.List;

/**
 * Business operations for training sessions.
 */
public interface TrainingService {

    /**
     * Adds a training for an authenticated trainee.
     *
     * @param traineeUsername trainee username
     * @param traineePassword trainee password
     * @param request training data
     */
    void addTraining(String traineeUsername, String traineePassword, AddTrainingRequest request);

    /**
     * Returns trainee trainings after authenticating the trainee credentials.
     *
     * @param username trainee username
     * @param password trainee password
     * @param criteria optional filters
     * @return trainee trainings matching the criteria
     */
    List<Training> getTraineeTrainings(String username, String password, TraineeTrainingCriteria criteria);

    /**
     * Returns trainer trainings after authenticating the trainer credentials.
     *
     * @param username trainer username
     * @param password trainer password
     * @param criteria optional filters
     * @return trainer trainings matching the criteria
     */
    List<Training> getTrainerTrainings(String username, String password, TrainerTrainingCriteria criteria);
}
