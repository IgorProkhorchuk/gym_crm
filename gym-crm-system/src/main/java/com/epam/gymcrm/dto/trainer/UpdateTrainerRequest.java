package com.epam.gymcrm.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainerRequest(
    @NotBlank(message = "Username must not be blank") String username,
    @NotBlank(message = "First name must not be blank") String firstName,
    @NotBlank(message = "Last name must not be blank") String lastName,
    @NotBlank(message = "Trainer specialization must not be blank") String specialization,
    @NotNull(message = "Active status must not be null") Boolean active) {

  @Override
  public String toString() {
    return "UpdateTrainerRequest[username=[PROTECTED], "
        + "firstName=[PROTECTED], lastName=[PROTECTED], specialization="
        + specialization
        + ", active="
        + active
        + "]";
  }
}
