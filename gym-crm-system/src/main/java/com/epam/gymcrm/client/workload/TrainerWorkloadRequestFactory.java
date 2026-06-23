package com.epam.gymcrm.client.workload;

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
    User trainerUser = training.getTrainer().getUser();
    return new TrainerWorkloadRequest(
        trainerUser.getUsername(),
        trainerUser.getFirstName(),
        trainerUser.getLastName(),
        trainerUser.getActive(),
        training.getTrainingDate(),
        training.getTrainingDuration(),
        actionType);
  }
}
