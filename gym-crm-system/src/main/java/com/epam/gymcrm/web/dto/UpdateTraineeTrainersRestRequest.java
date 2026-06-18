package com.epam.gymcrm.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateTraineeTrainersRestRequest(
    @NotBlank(message = "must not be blank") String traineeUsername,
    @NotEmpty(message = "must contain at least one trainer username")
        List<@NotBlank(message = "must not be blank") String> trainerUsernames) {

  @Override
  public String toString() {
    return "UpdateTraineeTrainersRestRequest[traineeUsername=[PROTECTED], "
        + "trainerUsernames=[PROTECTED]]";
  }
}
