package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

/** Business operations for training sessions. */
@Validated
public interface TrainingService {

  /**
   * Adds a training for an authenticated trainee.
   *
   * @param request training data
   */
  void addTraining(
      @Valid @NotNull(message = "Training request must not be null") AddTrainingRequest request);

  /**
   * Deletes training by id.
   *
   * @param trainingId training id
   */
  void deleteTraining(@NotNull(message = "Training id must not be null") Long trainingId);

  /**
   * Returns trainee trainings after authenticating the trainee credentials.
   *
   * @param request trainee credentials and optional filters
   * @return trainee trainings matching the criteria
   */
  List<TraineeTrainingResponse> getTraineeTrainings(
      @Valid @NotNull(message = "Trainee trainings request must not be null")
          TraineeTrainingsRequest request);

  /**
   * Returns trainer trainings after authenticating the trainer credentials.
   *
   * @param request trainer credentials and optional filters
   * @return trainer trainings matching the criteria
   */
  List<TrainerTrainingResponse> getTrainerTrainings(
      @Valid @NotNull(message = "Trainer trainings request must not be null")
          TrainerTrainingsRequest request);
}
