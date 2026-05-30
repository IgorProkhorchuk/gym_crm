package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.dto.auth.ProfileType;

public record AuthenticatedUser(String username, String password, ProfileType profileType) {

  @Override
  public String toString() {
    return "AuthenticatedUser[username=[PROTECTED], password=[PROTECTED]]";
  }
}
