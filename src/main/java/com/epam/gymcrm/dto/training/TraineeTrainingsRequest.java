package com.epam.gymcrm.dto.training;

import com.epam.gymcrm.dto.PageRequest;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record TraineeTrainingsRequest(
    @NotBlank(message = "Username must not be blank") String username,
    LocalDate fromDate,
    LocalDate toDate,
    String trainerName,
    String trainingType,
    PageRequest pageRequest) {

  @Override
  public String toString() {
    return "TraineeTrainingsRequest[username=[PROTECTED], "
        + "fromDate="
        + fromDate
        + ", toDate="
        + toDate
        + ", trainerName=[PROTECTED], trainingType="
        + trainingType
        + ", pageRequest="
        + pageRequest
        + "]";
  }
}
