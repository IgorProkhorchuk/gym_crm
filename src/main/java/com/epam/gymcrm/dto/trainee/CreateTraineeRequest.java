package com.epam.gymcrm.dto.trainee;

import java.time.LocalDate;

public record CreateTraineeRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        Boolean active) {

}
