package com.epam.gymcrm.dto.trainee;

import java.util.List;

public record UpdateTraineeTrainersRequest(String username,
                                           String password,
                                           List<String> trainerUsernames) {

    @Override
    public String toString() {
        return "UpdateTraineeTrainersRequest[username=[PROTECTED], password=[PROTECTED], "
                + "trainerUsernames=[PROTECTED]]";
    }
}
