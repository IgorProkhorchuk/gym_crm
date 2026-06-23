package com.epam.gymcrm.workload.dto;

import java.util.List;

public record TrainerWorkloadResponse(
    String trainerUsername,
    String trainerFirstName,
    String trainerLastName,
    boolean trainerStatus,
    List<TrainerWorkloadYearResponse> years
) {
}
