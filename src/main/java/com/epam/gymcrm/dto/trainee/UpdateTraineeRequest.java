package com.epam.gymcrm.dto.trainee;

import java.time.LocalDate;

public record UpdateTraineeRequest(
        String username,
        String password,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address
) {

    @Override
    public String toString() {
        return "UpdateTraineeRequest[username=[PROTECTED], password=[PROTECTED], "
                + "firstName=[PROTECTED], lastName=[PROTECTED], dateOfBirth=[PROTECTED], address=[PROTECTED]]";
    }
}
