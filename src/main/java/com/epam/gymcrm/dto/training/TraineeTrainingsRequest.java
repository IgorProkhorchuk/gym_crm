package com.epam.gymcrm.dto.training;

import java.time.LocalDate;

public record TraineeTrainingsRequest(
        String username,
        String password,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        String trainingType
) {
}
