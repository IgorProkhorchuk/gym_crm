package com.epam.gymcrm.web.dto;

public record UpdateTrainerProfileRestRequest(
    String username,
    String firstName,
    String lastName,
    String specialization,
    Boolean active
) {

  @Override
  public String toString() {
    return "UpdateTrainerProfileRestRequest[username=[PROTECTED], firstName=[PROTECTED], "
        + "lastName=[PROTECTED], specialization=" + specialization + ", active=" + active + "]";
  }
}
