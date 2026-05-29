package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.dto.auth.ProfileType;

public record AuthenticatedUser(
    String username, String password, ProfileType profileType, Long userId, Long profileId) {

  public AuthenticatedUser(String username, String password, ProfileType profileType) {
    this(username, password, profileType, null, null);
  }

  @Override
  public String toString() {
    return "AuthenticatedUser[username=[PROTECTED], password=[PROTECTED], profileType="
        + profileType
        + ", userId="
        + userId
        + ", profileId="
        + profileId
        + "]";
  }
}
