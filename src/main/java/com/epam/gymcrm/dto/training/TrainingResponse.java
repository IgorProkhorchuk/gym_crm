package com.epam.gymcrm.dto.training;

import java.time.LocalDate;

public record TrainingResponse(
        Long id,
        String trainingName,
        String trainingType,
        LocalDate trainingDate,
        Integer trainingDuration,
        String traineeUsername,
        String traineeFirstName,
        String traineeLastName,
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName
) {
}
