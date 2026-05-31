package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.util.SensitiveInfo;
import com.epam.gymcrm.util.SensitiveToString;

public record AuthenticatedUser(
    @SensitiveInfo String username, @SensitiveInfo String password, ProfileType profileType) {

  @Override
  public String toString() {
    return SensitiveToString.toString(this);
  }
}
