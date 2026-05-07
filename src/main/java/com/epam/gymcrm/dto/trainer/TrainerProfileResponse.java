package com.epam.gymcrm.dto.trainer;

public record TrainerProfileResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        Boolean active,
        String specialization
) {
}
