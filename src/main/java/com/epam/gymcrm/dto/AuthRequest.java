package com.epam.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @NotBlank(message = "Username must not be blank") String username) {

  @Override
  public String toString() {
    return "AuthRequest[username=[PROTECTED]]";
  }
}
