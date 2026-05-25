package com.epam.gymcrm.web.dto;

public record ChangePasswordRestRequest(String username, String oldPassword, String newPassword) {

  @Override
  public String toString() {
    return "ChangePasswordRestRequest[username=[PROTECTED], oldPassword=[PROTECTED], newPassword=[PROTECTED]]";
  }
}
