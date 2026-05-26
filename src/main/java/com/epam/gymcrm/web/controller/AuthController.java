package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.auth.LoginRequest;
import com.epam.gymcrm.dto.auth.LoginResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.FakeTokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/login")
public class AuthController {
  private final AuthenticationService authenticationService;
  private final FakeTokenService tokenService;

  public AuthController(
      AuthenticationService authenticationService, FakeTokenService tokenService) {
    this.authenticationService = authenticationService;
    this.tokenService = tokenService;
  }

  @PostMapping
  public LoginResponse loginUser(@RequestBody LoginRequest loginRequest) {
    try {
      authenticationService.authenticateTrainee(loginRequest.username(), loginRequest.password());
      AuthenticatedUser authenticatedUser =
          new AuthenticatedUser(
              loginRequest.username(), loginRequest.password(), ProfileType.TRAINEE);
      String token = tokenService.createToken(authenticatedUser);

      return new LoginResponse(token, ProfileType.TRAINEE);
    } catch (AuthenticationException exception) {
      authenticationService.authenticateTrainer(loginRequest.username(), loginRequest.password());

      AuthenticatedUser authenticatedUser =
          new AuthenticatedUser(
              loginRequest.username(), loginRequest.password(), ProfileType.TRAINER);
      String token = tokenService.createToken(authenticatedUser);

      return new LoginResponse(token, ProfileType.TRAINER);
    }
  }
}
