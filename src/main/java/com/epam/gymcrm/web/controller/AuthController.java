package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.auth.LoginRequest;
import com.epam.gymcrm.dto.auth.LoginResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AccountLockedException;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.monitoring.metrics.GymMetrics;
import com.epam.gymcrm.web.api.AuthApi;
import com.epam.gymcrm.web.auth.JwtRevocationService;
import com.epam.gymcrm.web.auth.JwtTokenService;
import com.epam.gymcrm.web.auth.LoginAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private static final String INVALID_CREDENTIALS_ERROR = "Invalid username or password";
  private static final String ACCOUNT_LOCKED_ERROR =
      "User is temporarily blocked because of too many failed login attempts. Try again later.";
  private static final String ROLE_PREFIX = "ROLE_";

  private final GymMetrics gymMetrics;
  private final JwtTokenService jwtTokenService;
  private final JwtRevocationService jwtRevocationService;
  private final AuthenticationManager authenticationManager;
  private final LoginAttemptService loginAttemptService;

  @PostMapping("/login")
  @Override
  public LoginResponse loginUser(@Valid @RequestBody LoginRequest loginRequest) {
    if (loginAttemptService.isBlocked(loginRequest.username())) {
      throw new AccountLockedException(ACCOUNT_LOCKED_ERROR);
    }

    try {
      Authentication authentication =
          authenticationManager.authenticate(
              UsernamePasswordAuthenticationToken.unauthenticated(
                  loginRequest.username(), loginRequest.password()));
      ProfileType profileType = resolveProfileType(authentication);
      String token = jwtTokenService.createToken(loginRequest.username(), profileType);
      loginAttemptService.loginSucceeded(loginRequest.username());

      return new LoginResponse(token, profileType);
    } catch (org.springframework.security.core.AuthenticationException exception) {
      gymMetrics.recordLoginFailedInvalidCredentials();
      if (loginAttemptService.loginFailed(loginRequest.username())) {
        throw new AccountLockedException(ACCOUNT_LOCKED_ERROR);
      }
      throw new AuthenticationException(INVALID_CREDENTIALS_ERROR);
    }
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Override
  public void logoutUser() {
    JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    jwtRevocationService.revoke(authentication.getToken());
  }

  private static ProfileType resolveProfileType(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> authority.startsWith(ROLE_PREFIX))
        .filter(
            authority ->
                (ROLE_PREFIX + ProfileType.TRAINEE.name()).equals(authority)
                    || (ROLE_PREFIX + ProfileType.TRAINER.name()).equals(authority))
        .map(authority -> authority.substring(ROLE_PREFIX.length()))
        .map(ProfileType::valueOf)
        .findFirst()
        .orElseThrow(() -> new AuthenticationException("Authenticated user profile not found"));
  }
}
