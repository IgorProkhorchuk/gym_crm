package com.epam.gymcrm.web.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dto.auth.LoginRequest;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.TokenService;
import com.epam.gymcrm.web.exception.RestExceptionHandler;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private static final String USERNAME = "John.Doe";
  private static final String PASSWORD = "password";

  @Mock private AuthenticationService authenticationService;

  @Mock private TokenService tokenService;

  @BeforeEach
  void setUp() {
    standaloneSetup(
        new AuthController(authenticationService, tokenService), new RestExceptionHandler());
  }

  @AfterEach
  void tearDown() {
    reset();
  }

  @Test
  void loginUserShouldReturnTraineeTokenWhenTraineeAuthenticationSucceeds() {
    LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
    AuthenticatedUser authenticatedUser =
        new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    when(authenticationService.authenticateTrainee(USERNAME, PASSWORD)).thenReturn(new Trainee());
    when(tokenService.createToken(authenticatedUser)).thenReturn("trainee-token");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(200)
        .body("token", equalTo("trainee-token"))
        .body("profileType", equalTo("TRAINEE"));

    verify(authenticationService).authenticateTrainee(USERNAME, PASSWORD);
    verify(tokenService).createToken(authenticatedUser);
  }

  @Test
  void
      loginUserShouldReturnTrainerTokenWhenTraineeAuthenticationFailsAndTrainerAuthenticationSucceeds() {
    LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
    AuthenticatedUser authenticatedUser =
        new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINER);
    when(authenticationService.authenticateTrainee(USERNAME, PASSWORD))
        .thenThrow(new AuthenticationException("Invalid username or password"));
    when(authenticationService.authenticateTrainer(USERNAME, PASSWORD)).thenReturn(new Trainer());
    when(tokenService.createToken(authenticatedUser)).thenReturn("trainer-token");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(200)
        .body("token", equalTo("trainer-token"))
        .body("profileType", equalTo("TRAINER"));

    verify(authenticationService).authenticateTrainee(USERNAME, PASSWORD);
    verify(authenticationService).authenticateTrainer(USERNAME, PASSWORD);
    verify(tokenService).createToken(authenticatedUser);
  }

  @Test
  void loginUserShouldReturnUnauthorizedWhenBothAuthenticationsFail() {
    LoginRequest request = new LoginRequest(USERNAME, "wrong-password");
    when(authenticationService.authenticateTrainee(USERNAME, "wrong-password"))
        .thenThrow(new AuthenticationException("Invalid username or password"));
    when(authenticationService.authenticateTrainer(USERNAME, "wrong-password"))
        .thenThrow(new AuthenticationException("Invalid username or password"));

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid username or password"));

    verifyNoInteractions(tokenService);
  }
}
