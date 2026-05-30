package com.epam.gymcrm.dto;

public record ChangePasswordRequest(String username, String oldPassword, String newPassword) {

  @Override
  public String toString() {
    return "ChangePasswordRequest[username=[PROTECTED], oldPassword=[PROTECTED], newPassword=[PROTECTED]]";
  }
}
