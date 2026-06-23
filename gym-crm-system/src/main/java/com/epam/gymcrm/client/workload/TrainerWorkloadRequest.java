package com.epam.gymcrm.client.workload;

import java.time.LocalDate;

public record TrainerWorkloadRequest(
    String trainerUsername,
    String trainerFirstName,
    String trainerLastName,
    Boolean trainerStatus,
    LocalDate trainingDate,
    Integer trainingDuration,
    TrainerWorkloadActionType actionType
) {
}
