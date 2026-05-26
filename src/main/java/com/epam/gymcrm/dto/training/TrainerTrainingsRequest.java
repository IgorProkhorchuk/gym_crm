package com.epam.gymcrm.dto.training;

import com.epam.gymcrm.dto.PageRequest;
import java.time.LocalDate;

public record TrainerTrainingsRequest(
    String username,
    String password,
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
