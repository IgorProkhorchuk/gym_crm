package com.epam.gymcrm.dto.trainer;

import jakarta.validation.constraints.NotBlank;

public record CreateTrainerRequest(
    @NotBlank(message = "must not be blank") String firstName,
    @NotBlank(message = "must not be blank") String lastName,
    @NotBlank(message = "must not be blank") String specialization) {

  @Override
  public String toString() {
    return "CreateTrainerRequest[firstName=[PROTECTED], lastName=[PROTECTED], specialization="
        + specialization
        + "]";
  }
}
