package com.epam.gymcrm.dto.trainer;

public record TrainerSummaryResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String specialization
) {
}
