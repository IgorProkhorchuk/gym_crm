package com.epam.gymcrm.dto.training;

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
    String traineeUsername,
    String traineePassword,
    String trainerUsername,
    String trainingName,
    String trainingTypeName,
    LocalDate trainingDate,
    Integer trainingDuration) {

  @Override
  public String toString() {
    return "AddTrainingRequest[traineeUsername=[PROTECTED], traineePassword=[PROTECTED], "
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
