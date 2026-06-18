package com.epam.gymcrm.dto.trainer;

public record TrainerSummaryResponse(
    String username, String firstName, String lastName, String specialization) {

  @Override
  public String toString() {
    return "TrainerSummaryResponse[username=[PROTECTED], firstName=[PROTECTED], lastName=[PROTECTED], "
        + "specialization="
        + specialization
        + "]";
  }
}
