package com.epam.gymcrm.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRestRequest(
    @NotBlank(message = "must not be blank") String username,
    @NotBlank(message = "must not be blank") String oldPassword,
    @NotBlank(message = "must not be blank") String newPassword) {

  @Override
  public String toString() {
    return "ChangePasswordRestRequest[username=[PROTECTED], oldPassword=[PROTECTED], newPassword=[PROTECTED]]";
  }
}
