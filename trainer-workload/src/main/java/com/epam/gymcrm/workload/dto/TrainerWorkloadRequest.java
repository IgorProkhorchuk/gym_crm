package com.epam.gymcrm.workload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TrainerWorkloadRequest(
    @NotBlank String trainerUsername,
    @NotBlank String trainerFirstName,
    @NotBlank String trainerLastName,
    @NotNull Boolean trainerStatus,
    @NotNull LocalDate trainingDate,
    @Min(1) int trainingDuration,
    @NotNull ActionType actionType
) {

}
