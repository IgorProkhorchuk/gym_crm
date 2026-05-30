package com.epam.gymcrm.dto;

public record AuthRequest(String username, String password) {

  @Override
  public String toString() {
    return "AuthRequest[username=[PROTECTED], password=[PROTECTED]]";
  }
}
