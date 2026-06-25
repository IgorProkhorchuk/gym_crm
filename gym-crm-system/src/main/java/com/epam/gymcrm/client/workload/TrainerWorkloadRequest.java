package com.epam.gymcrm.client.workload;

import java.time.LocalDate;

public record TrainerWorkloadRequest(
    Long trainingId,
    String trainerUsername,
    String trainerFirstName,
    String trainerLastName,
    Boolean trainerStatus,
    LocalDate trainingDate,
    Integer trainingDuration,
    TrainerWorkloadActionType actionType
) {
}
