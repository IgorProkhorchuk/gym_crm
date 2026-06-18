package com.epam.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank(message = "Username must not be blank") String username,
    @NotBlank(message = "Old password must not be blank") String oldPassword,
    @NotBlank(message = "New password must not be blank") String newPassword) {

  @Override
  public String toString() {
    return "ChangePasswordRequest[username=[PROTECTED], oldPassword=[PROTECTED], newPassword=[PROTECTED]]";
  }
}
