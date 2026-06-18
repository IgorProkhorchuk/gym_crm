package com.epam.gymcrm.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateTraineeProfileRestRequest(
    @NotBlank(message = "must not be blank") String username,
    @NotBlank(message = "must not be blank") String firstName,
    @NotBlank(message = "must not be blank") String lastName,
    LocalDate dateOfBirth,
    String address,
    @NotNull(message = "must not be null") Boolean active) {
  @Override
  public String toString() {
    return "UpdateTraineeProfileRestRequest[username=[PROTECTED], firstName=[PROTECTED], "
        + "lastName=[PROTECTED], dateOfBirth=[PROTECTED], address=[PROTECTED], active="
        + active
        + "]";
  }
}
