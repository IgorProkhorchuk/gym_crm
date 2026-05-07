package com.epam.gymcrm.dto.training;

import java.time.LocalDate;

public record TrainerTrainingsRequest(
        String username,
        String password,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {
}
