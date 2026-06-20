package com.epam.gymcrm.workload.dto;

import java.util.List;

public record TrainerWorkloadYearResponse(
    int year,
    List<TrainerWorkloadMonthResponse> months
) {
}
