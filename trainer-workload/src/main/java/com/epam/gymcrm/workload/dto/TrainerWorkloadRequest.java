package com.epam.gymcrm.workload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TrainerWorkloadRequest(
    @NotNull Long trainingId,
    @NotBlank String trainerUsername,
    @NotBlank String trainerFirstName,
    @NotBlank String trainerLastName,
    @NotNull Boolean trainerStatus,
    @NotNull LocalDate trainingDate,
    @Min(1) int trainingDuration,
    @NotNull ActionType actionType
) {

  @Override
  public String toString() {
    return "TrainerWorkloadRequest[trainingId="
        + trainingId
        + ", trainerUsername=[PROTECTED], trainerFirstName=[PROTECTED], "
        + "trainerLastName=[PROTECTED], trainerStatus="
        + trainerStatus
        + ", trainingDate="
        + trainingDate
        + ", trainingDuration="
        + trainingDuration
        + ", actionType="
        + actionType
        + "]";
  }
}
