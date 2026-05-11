package com.epam.gymcrm.dto.trainer;

public record CreateTrainerRequest(
        String firstName,
        String lastName,
        String specialization) {

    @Override
    public String toString() {
        return "CreateTrainerRequest[firstName=[PROTECTED], lastName=[PROTECTED], specialization=" + specialization + "]";
    }
}
