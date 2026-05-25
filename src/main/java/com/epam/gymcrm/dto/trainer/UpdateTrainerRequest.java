package com.epam.gymcrm.dto.trainer;

public record UpdateTrainerRequest(
        String username,
        String password,
        String firstName,
        String lastName,
        String specialization,
        Boolean active
) {

    @Override
    public String toString() {
        return "UpdateTrainerRequest[username=[PROTECTED], password=[PROTECTED], "
                + "firstName=[PROTECTED], lastName=[PROTECTED], specialization=" + specialization
                + ", active=" + active + "]";
    }
}
