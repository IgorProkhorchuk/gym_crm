package com.epam.gymcrm.client.workload;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonNull;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.User;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadRequestFactory {

  /**
   * Creates trainer workload request from training and update action.
   *
   * @param training source training
   * @param actionType workload update action
   * @return trainer workload request
   */
  public TrainerWorkloadRequest fromTraining(
      Training training,
      TrainerWorkloadActionType actionType
  ) {
    Training sourceTraining = requireNonNull(training, "Training must not be null");
    Trainer trainer = requireNonNull(sourceTraining.getTrainer(), "Training trainer must not be null");
    User trainerUser = requireNonNull(trainer.getUser(), "Trainer user must not be null");
    return new TrainerWorkloadRequest(
        sourceTraining.getTrainingId(),
        trainerUser.getUsername(),
        trainerUser.getFirstName(),
        trainerUser.getLastName(),
        trainerUser.getActive(),
        sourceTraining.getTrainingDate(),
        sourceTraining.getTrainingDuration(),
        actionType);
  }
}
