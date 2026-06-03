package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.auth.LoginRequest;
import com.epam.gymcrm.dto.auth.LoginResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.monitoring.metrics.GymMetrics;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.web.api.AuthApi;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/login")
@RequiredArgsConstructor
public class AuthController implements AuthApi {
  private final AuthenticationService authenticationService;
  private final GymMetrics gymMetrics;
  private final TokenService tokenService;

  @PostMapping
  @Override
  public LoginResponse loginUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      authenticationService.authenticateTrainee(loginRequest.username(), loginRequest.password());
      AuthenticatedUser authenticatedUser =
          new AuthenticatedUser(
              loginRequest.username(), loginRequest.password(), ProfileType.TRAINEE);
      String token = tokenService.createToken(authenticatedUser);

      return new LoginResponse(token, ProfileType.TRAINEE);
    } catch (AuthenticationException exception) {
      try {
        authenticationService.authenticateTrainer(loginRequest.username(), loginRequest.password());
      } catch (AuthenticationException trainerException) {
        gymMetrics.recordLoginFailedInvalidCredentials();
        throw trainerException;
      }

      AuthenticatedUser authenticatedUser =
          new AuthenticatedUser(
              loginRequest.username(), loginRequest.password(), ProfileType.TRAINER);
      String token = tokenService.createToken(authenticatedUser);

      return new LoginResponse(token, ProfileType.TRAINER);
    }
  }
}
