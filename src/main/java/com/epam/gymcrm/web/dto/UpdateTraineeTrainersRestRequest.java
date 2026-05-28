package com.epam.gymcrm.web.dto;

import java.util.List;

public record UpdateTraineeTrainersRestRequest(
    String traineeUsername, List<String> trainerUsernames) {

  @Override
  public String toString() {
    return "UpdateTraineeTrainersRestRequest[traineeUsername=[PROTECTED], "
        + "trainerUsernames=[PROTECTED]]";
  }
}
