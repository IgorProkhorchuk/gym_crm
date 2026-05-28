package com.epam.gymcrm.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record AddTrainingRestRequest(
    @NotBlank(message = "must not be blank") String traineeUsername,
    @NotBlank(message = "must not be blank") String trainerUsername,
    @NotBlank(message = "must not be blank") String trainingName,
    @NotBlank(message = "must not be blank") String trainingTypeName,
    @NotNull(message = "must not be null") LocalDate trainingDate,
    @NotNull(message = "must not be null")
        @Positive(message = "must be positive")
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
