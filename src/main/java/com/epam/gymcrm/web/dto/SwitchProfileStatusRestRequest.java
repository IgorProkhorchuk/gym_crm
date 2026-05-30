package com.epam.gymcrm.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SwitchProfileStatusRestRequest(
    @NotBlank(message = "must not be blank") String username,
    @NotNull(message = "must not be null") Boolean active) {

  @Override
  public String toString() {
    return "SwitchProfileStatusRestRequest[username=[PROTECTED], active=" + active + "]";
  }
}
