package com.epam.gymcrm.web.dto;

public record DeleteProfileRestRequest(String username) {

  @Override
  public String toString() {
    return "DeleteProfileRestRequest[username=[PROTECTED]]";
  }
}
