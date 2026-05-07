package com.epam.gymcrm.dto.trainer;

public record UpdateTrainerRequest(String username,
                                   String password,
                                   Long id,
                                   String firstName,
                                   String lastName,
                                   String specialization
) {
}
