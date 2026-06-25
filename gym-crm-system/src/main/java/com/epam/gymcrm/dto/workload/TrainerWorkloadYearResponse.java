package com.epam.gymcrm.dto.workload;

import java.util.List;

public record TrainerWorkloadYearResponse(
    int year,
    List<TrainerWorkloadMonthResponse> months
) {
}
