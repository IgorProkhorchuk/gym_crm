package com.epam.gymcrm.dto.training;

import java.time.LocalDate;

public record TrainerTrainingResponse(
    String trainingName,
    String trainingType,
    LocalDate trainingDate,
    Integer trainingDuration,
    String traineeName) {

  @Override
  public String toString() {
    return "TrainerTrainingResponse[trainingName="
        + trainingName
        + ", trainingType="
        + trainingType
        + ", trainingDate="
        + trainingDate
        + ", trainingDuration="
        + trainingDuration
        + ", traineeName=[PROTECTED]]";
  }
}
