package com.epam.gymcrm.dto.trainee;

import java.time.LocalDate;
import java.util.List;

import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;

public record TraineeProfileResponse(
        String username,
        String firstName,
        String lastName,
        Boolean active,
        LocalDate dateOfBirth,
        String address,
        List<TrainerSummaryResponse> trainers
) {

    @Override
    public String toString() {
        return "TraineeProfileResponse[username=[PROTECTED], firstName=[PROTECTED], lastName=[PROTECTED], "
                + "active=" + active + ", dateOfBirth=[PROTECTED], address=[PROTECTED], trainers=" + trainers + "]";
    }
}
