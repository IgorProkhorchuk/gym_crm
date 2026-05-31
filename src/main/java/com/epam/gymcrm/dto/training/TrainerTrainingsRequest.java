package com.epam.gymcrm.dto.training;

import com.epam.gymcrm.dto.PageRequest;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record TrainerTrainingsRequest(
    @NotBlank(message = "Username must not be blank") String username,
    @NotBlank(message = "Password must not be blank") String password,
    LocalDate fromDate,
    LocalDate toDate,
    String traineeName,
    PageRequest pageRequest) {

  @Override
  public String toString() {
    return "TrainerTrainingsRequest[username=[PROTECTED], password=[PROTECTED], "
        + "fromDate="
        + fromDate
        + ", toDate="
        + toDate
        + ", traineeName=[PROTECTED], pageRequest="
        + pageRequest
        + "]";
  }
}
