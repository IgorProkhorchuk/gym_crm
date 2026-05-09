package com.epam.gymcrm.dto.training;

import com.epam.gymcrm.dto.PageRequest;

import java.time.LocalDate;

public record TraineeTrainingsRequest(
        String username,
        String password,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        String trainingType,
        PageRequest pageRequest
) {

    @Override
    public String toString() {
        return "TraineeTrainingsRequest[username=[PROTECTED], password=[PROTECTED], "
                + "fromDate=" + fromDate
                + ", toDate=" + toDate
                + ", trainerName=[PROTECTED], trainingType=" + trainingType
                + ", pageRequest=" + pageRequest + "]";
    }
}
