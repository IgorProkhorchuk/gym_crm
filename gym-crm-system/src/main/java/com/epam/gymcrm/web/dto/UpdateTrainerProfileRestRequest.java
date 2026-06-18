package com.epam.gymcrm.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainerProfileRestRequest(
    @NotBlank(message = "must not be blank") String username,
    @NotBlank(message = "must not be blank") String firstName,
    @NotBlank(message = "must not be blank") String lastName,
    @NotBlank(message = "must not be blank") String specialization,
    @NotNull(message = "must not be null") Boolean active) {

  @Override
  public String toString() {
    return "UpdateTrainerProfileRestRequest[username=[PROTECTED], firstName=[PROTECTED], "
        + "lastName=[PROTECTED], specialization="
        + specialization
        + ", active="
        + active
        + "]";
  }
}
