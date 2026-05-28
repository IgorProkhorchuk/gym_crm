package com.epam.gymcrm.web.dto;

public record SwitchProfileStatusRestRequest(String username, Boolean active) {

  @Override
  public String toString() {
    return "SwitchProfileStatusRestRequest[username=[PROTECTED], active=" + active + "]";
  }
}
