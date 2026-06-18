package com.epam.gymcrm.dto.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * Request data required to add a training for an authenticated trainee.
 *
 * @param trainerUsername trainer username
 * @param trainingName training name
 * @param trainingTypeName existing training type name
 * @param trainingDate training date
 * @param trainingDuration training duration in minutes
 */
public record AddTrainingRequest(
    @NotBlank(message = "Trainee username must not be blank") String traineeUsername,
    @NotBlank(message = "Trainer username must not be blank") String trainerUsername,
    @NotBlank(message = "Training name must not be blank") String trainingName,
    @NotBlank(message = "Training type must not be blank") String trainingTypeName,
    @NotNull(message = "Training date must not be null") LocalDate trainingDate,
    @NotNull(message = "Training duration must not be null")
        @Positive(message = "Training duration must be positive")
        Integer trainingDuration) {

  @Override
  public String toString() {
    return "AddTrainingRequest[traineeUsername=[PROTECTED], "
        + "trainerUsername=[PROTECTED], trainingName="
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
