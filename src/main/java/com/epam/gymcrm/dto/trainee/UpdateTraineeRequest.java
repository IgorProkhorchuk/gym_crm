package com.epam.gymcrm.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateTraineeRequest(
    @NotBlank(message = "Username must not be blank") String username,
    @NotBlank(message = "Password must not be blank") String password,
    @NotBlank(message = "First name must not be blank") String firstName,
    @NotBlank(message = "Last name must not be blank") String lastName,
    LocalDate dateOfBirth,
    String address,
    @NotNull(message = "Active status must not be null") Boolean active) {

  @Override
  public String toString() {
    return "UpdateTraineeRequest[username=[PROTECTED], password=[PROTECTED], "
        + "firstName=[PROTECTED], lastName=[PROTECTED], dateOfBirth=[PROTECTED], "
        + "address=[PROTECTED], active="
        + active
        + "]";
  }
}
