package com.epam.gymcrm.dto.trainee;

import java.time.LocalDate;
import java.util.List;

import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;

public record TraineeProfileResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        Boolean active,
        LocalDate dateOfBirth,
        String address,
        List<TrainerSummaryResponse> trainers
) {
}
