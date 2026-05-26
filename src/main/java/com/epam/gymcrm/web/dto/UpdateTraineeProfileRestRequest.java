package com.epam.gymcrm.web.dto;

import java.time.LocalDate;

public record UpdateTraineeProfileRestRequest(
    String username,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String address,
    Boolean active) {
  @Override
  public String toString() {
    return "UpdateTraineeProfileRestRequest[username=[PROTECTED], firstName=[PROTECTED], "
        + "lastName=[PROTECTED], dateOfBirth=[PROTECTED], address=[PROTECTED], active="
        + active
        + "]";
  }
}
