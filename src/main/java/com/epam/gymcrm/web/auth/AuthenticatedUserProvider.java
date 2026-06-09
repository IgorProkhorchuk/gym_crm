package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AuthenticationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

  private static final String AUTHENTICATION_REQUIRED = "Authentication is required";
  private static final String PROFILE_NOT_FOUND = "Authenticated user profile not found";

  public AuthenticatedPrincipal currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      throw new AuthenticationException(AUTHENTICATION_REQUIRED);
    }

    return new AuthenticatedPrincipal(authentication.getName(), profileType(authentication));
  }

  private ProfileType profileType(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .map(this::profileType)
        .filter(profileType -> profileType != null)
        .findFirst()
        .orElseThrow(() -> new AuthenticationException(PROFILE_NOT_FOUND));
  }

  private ProfileType profileType(String authority) {
    return switch (authority) {
      case "ROLE_TRAINEE" -> ProfileType.TRAINEE;
      case "ROLE_TRAINER" -> ProfileType.TRAINER;
      default -> null;
    };
  }
}
