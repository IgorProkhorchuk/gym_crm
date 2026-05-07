package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainingResponse;

import java.util.List;

/**
 * Business operations for training sessions.
 */
public interface TrainingService {

    /**
     * Adds a training for an authenticated trainee.
     *
     * @param request training data
     */
    void addTraining(AddTrainingRequest request);

    /**
     * Returns trainee trainings after authenticating the trainee credentials.
     *
     * @param request trainee credentials and optional filters
     * @return trainee trainings matching the criteria
     */
    List<TrainingResponse> getTraineeTrainings(TraineeTrainingsRequest request);

    /**
     * Returns trainer trainings after authenticating the trainer credentials.
     *
     * @param request trainer credentials and optional filters
     * @return trainer trainings matching the criteria
     */
    List<TrainingResponse> getTrainerTrainings(TrainerTrainingsRequest request);
}
