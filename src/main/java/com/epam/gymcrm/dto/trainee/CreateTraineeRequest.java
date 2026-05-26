package com.epam.gymcrm.dto.trainee;

import java.time.LocalDate;

public record CreateTraineeRequest(
    String firstName, String lastName, LocalDate dateOfBirth, String address) {

  @Override
  public String toString() {
    return "CreateTraineeRequest[firstName=[PROTECTED], lastName=[PROTECTED], "
        + "dateOfBirth=[PROTECTED], address=[PROTECTED]]";
  }
}
