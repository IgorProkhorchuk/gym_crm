package com.epam.gymcrm.dto;

public record UsernamePasswordResponse(String username, String password) {

  @Override
  public String toString() {
    return "UsernamePasswordResponse[username=[PROTECTED], password=[PROTECTED]]";
  }
}
