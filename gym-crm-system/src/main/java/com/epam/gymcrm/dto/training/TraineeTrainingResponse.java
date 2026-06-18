package com.epam.gymcrm.dto.training;

import java.time.LocalDate;

public record TraineeTrainingResponse(
    String trainingName,
    String trainingType,
    LocalDate trainingDate,
    Integer trainingDuration,
    String trainerName) {

  @Override
  public String toString() {
    return "TraineeTrainingResponse[trainingName="
        + trainingName
        + ", trainingType="
        + trainingType
        + ", trainingDate="
        + trainingDate
        + ", trainingDuration="
        + trainingDuration
        + ", trainerName=[PROTECTED]]";
  }
}
