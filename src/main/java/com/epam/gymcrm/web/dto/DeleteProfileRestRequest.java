package com.epam.gymcrm.web.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteProfileRestRequest(
    @NotBlank(message = "must not be blank") String username) {

  @Override
  public String toString() {
    return "DeleteProfileRestRequest[username=[PROTECTED]]";
  }
}
