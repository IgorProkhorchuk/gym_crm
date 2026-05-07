package com.epam.gymcrm.dto.trainer;

public record CreateTrainerRequest(
        String firstName,
        String lastName,
        String specialization,
        Boolean active) {

}
