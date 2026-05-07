package com.epam.gymcrm.dto.trainee;

import java.time.LocalDate;

public record UpdateTraineeRequest(String username,
                                   String password,
                                   Long id,
                                   String firstName,
                                   String lastName,
                                   LocalDate dateOfBirth,
                                   String address) {
}
