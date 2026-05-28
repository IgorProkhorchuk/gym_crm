package com.epam.gymcrm.web.dto;

import java.time.LocalDate;

public record AddTrainingRestRequest(
    String traineeUsername,
    String trainerUsername,
    String trainingName,
    String trainingTypeName,
    LocalDate trainingDate,
    Integer trainingDuration) {

  @Override
  public String toString() {
    return "AddTrainingRestRequest[traineeUsername=[PROTECTED], trainerUsername=[PROTECTED], "
        + "trainingName="
        + trainingName
        + ", trainingTypeName="
        + trainingTypeName
        + ", trainingDate="
        + trainingDate
        + ", trainingDuration="
        + trainingDuration
        + "]";
  }
}
