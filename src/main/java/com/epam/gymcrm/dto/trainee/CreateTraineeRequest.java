package com.epam.gymcrm.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateTraineeRequest(
    @NotBlank(message = "must not be blank") String firstName,
    @NotBlank(message = "must not be blank") String lastName,
    LocalDate dateOfBirth,
    String address) {

  @Override
  public String toString() {
    return "CreateTraineeRequest[firstName=[PROTECTED], lastName=[PROTECTED], "
        + "dateOfBirth=[PROTECTED], address=[PROTECTED]]";
  }
}
