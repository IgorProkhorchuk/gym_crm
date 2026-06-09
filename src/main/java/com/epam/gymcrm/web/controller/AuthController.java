package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.auth.LoginRequest;
import com.epam.gymcrm.dto.auth.LoginResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.monitoring.metrics.GymMetrics;
import com.epam.gymcrm.web.api.AuthApi;
import com.epam.gymcrm.web.auth.JwtTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/login")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private static final String INVALID_CREDENTIALS_ERROR = "Invalid username or password";
  private static final String ROLE_PREFIX = "ROLE_";

  private final GymMetrics gymMetrics;
  private final JwtTokenService jwtTokenService;
  private final AuthenticationManager authenticationManager;

  @PostMapping
  @Override
  public LoginResponse loginUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              UsernamePasswordAuthenticationToken.unauthenticated(
                  loginRequest.username(), loginRequest.password()));
      ProfileType profileType = resolveProfileType(authentication);
      String token = jwtTokenService.createToken(loginRequest.username(), profileType);

      return new LoginResponse(token, profileType);
    } catch (org.springframework.security.core.AuthenticationException exception) {
      gymMetrics.recordLoginFailedInvalidCredentials();
      throw new AuthenticationException(INVALID_CREDENTIALS_ERROR);
    }
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
