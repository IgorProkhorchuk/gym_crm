package com.epam.gymcrm.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateTraineeTrainersRequest(
    @NotBlank(message = "Username must not be blank") String username,
    @NotBlank(message = "Password must not be blank") String password,
    @NotNull(message = "Trainer usernames must not be null")
        List<@NotBlank(message = "Trainer username must not be blank") String> trainerUsernames) {

  @Override
  public String toString() {
    return "UpdateTraineeTrainersRequest[username=[PROTECTED], password=[PROTECTED], "
        + "trainerUsernames=[PROTECTED]]";
  }
}
