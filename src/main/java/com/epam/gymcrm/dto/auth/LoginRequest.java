package com.epam.gymcrm.dto.auth;


public record LoginRequest(String username, String password) {

  @Override
  public String toString() {
    return "LoginRequest[username=[PROTECTED], password=[PROTECTED]]";
  }
}
