package com.epam.gymcrm.dto.trainer;

public record TrainerProfileResponse(
    String username, String firstName, String lastName, Boolean active, String specialization) {

  @Override
  public String toString() {
    return "TrainerProfileResponse[username=[PROTECTED], firstName=[PROTECTED], lastName=[PROTECTED], "
        + "active="
        + active
        + ", specialization="
        + specialization
        + "]";
  }
}
