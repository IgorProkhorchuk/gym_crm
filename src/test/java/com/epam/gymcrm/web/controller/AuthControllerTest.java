package com.epam.gymcrm.web.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dto.auth.LoginRequest;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.monitoring.metrics.GymMetrics;
import com.epam.gymcrm.web.auth.JwtTokenService;
import com.epam.gymcrm.web.auth.LoginAttemptService;
import com.epam.gymcrm.web.exception.RestExceptionHandler;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private static final String USERNAME = "John.Doe";
  private static final String PASSWORD = "password";

  @Mock private JwtTokenService jwtTokenService;

  @Mock private GymMetrics gymMetrics;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private LoginAttemptService loginAttemptService;

  @BeforeEach
  void setUp() {
    standaloneSetup(
        new AuthController(gymMetrics, jwtTokenService, authenticationManager, loginAttemptService),
        new RestExceptionHandler());
  }

  @AfterEach
  void tearDown() {
    reset();
  }

  @Test
  void loginUserShouldReturnTraineeTokenWhenTraineeAuthenticationSucceeds() {
    LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication("ROLE_TRAINEE"));
    when(jwtTokenService.createToken(USERNAME, ProfileType.TRAINEE)).thenReturn("trainee-token");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(200)
        .body("token", equalTo("trainee-token"))
        .body("profileType", equalTo("TRAINEE"));

    verify(authenticationManager).authenticate(credentialsToken());
    verify(jwtTokenService).createToken(USERNAME, ProfileType.TRAINEE);
    verify(loginAttemptService).loginSucceeded(USERNAME);
    verifyNoInteractions(gymMetrics);
  }

  @Test
  void loginUserShouldReturnTrainerTokenWhenTrainerAuthenticationSucceeds() {
    LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication("ROLE_TRAINER"));
    when(jwtTokenService.createToken(USERNAME, ProfileType.TRAINER)).thenReturn("trainer-token");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(200)
        .body("token", equalTo("trainer-token"))
        .body("profileType", equalTo("TRAINER"));

    verify(authenticationManager).authenticate(credentialsToken());
    verify(jwtTokenService).createToken(USERNAME, ProfileType.TRAINER);
    verify(loginAttemptService).loginSucceeded(USERNAME);
    verifyNoInteractions(gymMetrics);
  }

  @Test
  void loginUserShouldReturnUnauthorizedWhenAuthenticationFails() {
    LoginRequest request = new LoginRequest(USERNAME, "wrong-password");
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid username or password"));

    verifyNoInteractions(jwtTokenService);
    verify(gymMetrics).recordLoginFailedInvalidCredentials();
    verify(loginAttemptService).loginFailed(USERNAME);
  }

  @Test
  void loginUserShouldReturnLockedWhenUserIsAlreadyBlocked() {
    LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
    when(loginAttemptService.isBlocked(USERNAME)).thenReturn(true);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(423)
        .body(
            "message",
            equalTo(
                "User is temporarily blocked because of too many failed login attempts. "
                    + "Try again later."));

    verifyNoInteractions(authenticationManager, jwtTokenService, gymMetrics);
  }

  @Test
  void loginUserShouldReturnLockedWhenFailedLoginReachesLimit() {
    LoginRequest request = new LoginRequest(USERNAME, "wrong-password");
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));
    when(loginAttemptService.loginFailed(USERNAME)).thenReturn(true);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(423)
        .body(
            "message",
            equalTo(
                "User is temporarily blocked because of too many failed login attempts. "
                    + "Try again later."));

    verifyNoInteractions(jwtTokenService);
    verify(gymMetrics).recordLoginFailedInvalidCredentials();
    verify(loginAttemptService).loginFailed(USERNAME);
  }

  @Test
  void loginUserShouldReturnUnauthorizedWhenAuthenticatedPrincipalHasNoSupportedProfile() {
    LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication("SCOPE_read", "ROLE_ADMIN"));

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/v1/auth/login")
        .then()
        .statusCode(401)
        .body("message", equalTo("Authenticated user profile not found"));

    verifyNoInteractions(jwtTokenService, gymMetrics);
  }

  private static Authentication authentication(String role) {
    return authentication(new String[] {role});
  }

  private static Authentication authentication(String... roles) {
    return new UsernamePasswordAuthenticationToken(
        USERNAME,
        PASSWORD,
        List.of(roles).stream().map(SimpleGrantedAuthority::new).toList());
  }

  private static UsernamePasswordAuthenticationToken credentialsToken() {
    return argThat(
        authentication ->
            USERNAME.equals(authentication.getPrincipal())
                && PASSWORD.equals(authentication.getCredentials()));
  }
}
