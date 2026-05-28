package com.epam.gymcrm.web.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.FakeTokenService;
import com.epam.gymcrm.web.exception.RestExceptionHandler;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

  private static final String TOKEN = "token";
  private static final String USERNAME = "Mike.Stone";
  private static final String PASSWORD = "password";

  @Mock private GymFacade gymFacade;

  @Mock private FakeTokenService fakeTokenService;

  @BeforeEach
  void setUp() {
    standaloneSetup(new TrainerController(gymFacade, fakeTokenService), new RestExceptionHandler());
  }

  @AfterEach
  void tearDown() {
    reset();
  }

  @Test
  void createTrainerShouldReturnCreatedCredentials() {
    CreateTrainerRequest request = new CreateTrainerRequest("Mike", "Stone", "Fitness");
    UsernamePasswordResponse response = new UsernamePasswordResponse(USERNAME, PASSWORD);
    when(gymFacade.createTrainer(request)).thenReturn(response);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                        {
                          "firstName": "Mike",
                          "lastName": "Stone",
                          "specialization": "Fitness"
                        }
            """)
        .when()
        .post("/v1/trainers")
        .then()
        .statusCode(201)
        .body("username", equalTo(USERNAME))
        .body("password", equalTo(PASSWORD));

    verify(gymFacade).createTrainer(request);
  }

  @Test
  void createTrainerShouldReturnBadRequestWhenRequestIsInvalid() {
    CreateTrainerRequest request = new CreateTrainerRequest("", "Stone", "Fitness");
    when(gymFacade.createTrainer(request))
        .thenThrow(new IllegalArgumentException("First name must not be blank"));

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                        {
                          "firstName": "",
                          "lastName": "Stone",
                          "specialization": "Fitness"
                        }
            """)
        .when()
        .post("/v1/trainers")
        .then()
        .statusCode(400)
        .body("message", equalTo("First name must not be blank"));

    verify(gymFacade).createTrainer(request);
  }

  @Test
  void getTrainerProfileShouldReturnProfileForTrainerToken() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINER);
    AuthRequest request = new AuthRequest(USERNAME, PASSWORD);
    TrainerProfileResponse response =
        new TrainerProfileResponse(USERNAME, "Mike", "Stone", true, "Fitness");
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);
    when(gymFacade.getTrainerProfile(request)).thenReturn(response);

    given()
        .header("X-Auth-Token", TOKEN)
        .when()
        .get("/v1/trainers/profile")
        .then()
        .statusCode(200)
        .body("username", equalTo(USERNAME))
        .body("firstName", equalTo("Mike"))
        .body("lastName", equalTo("Stone"))
        .body("active", equalTo(true))
        .body("specialization", equalTo("Fitness"));

    verify(fakeTokenService).getUserByToken(TOKEN);
    verify(gymFacade).getTrainerProfile(request);
  }

  @Test
  void getTrainerProfileShouldRejectTraineeToken() {
    AuthenticatedUser user = new AuthenticatedUser("John.Doe", PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .header("X-Auth-Token", TOKEN)
        .when()
        .get("/v1/trainers/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getTrainerProfileShouldRejectInvalidToken() {
    when(fakeTokenService.getUserByToken("invalid-token"))
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .header("X-Auth-Token", "invalid-token")
        .when()
        .get("/v1/trainers/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTrainerProfileShouldReturnUpdatedProfile() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINER);
    UpdateTrainerRequest request =
        new UpdateTrainerRequest(USERNAME, PASSWORD, "Michael", "Stone", "Yoga", false);
    TrainerProfileResponse response =
        new TrainerProfileResponse(USERNAME, "Michael", "Stone", false, "Yoga");
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);
    when(gymFacade.updateTrainer(request)).thenReturn(response);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Mike.Stone",
                          "firstName": "Michael",
                          "lastName": "Stone",
                          "specialization": "Yoga",
                          "active": false
                        }
            """)
        .when()
        .put("/v1/trainers/profile")
        .then()
        .statusCode(200)
        .body("username", equalTo(USERNAME))
        .body("firstName", equalTo("Michael"))
        .body("lastName", equalTo("Stone"))
        .body("active", equalTo(false))
        .body("specialization", equalTo("Yoga"));

    verify(fakeTokenService).getUserByToken(TOKEN);
    verify(gymFacade).updateTrainer(request);
  }

  @Test
  void updateTrainerProfileShouldRejectTraineeToken() {
    AuthenticatedUser user = new AuthenticatedUser("John.Doe", PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "John.Doe",
                          "firstName": "John",
                          "lastName": "Doe",
                          "specialization": "Yoga",
                          "active": true
                        }
            """)
        .when()
        .put("/v1/trainers/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTrainerProfileShouldRejectAnotherUsername() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINER);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Another.User",
                          "firstName": "Michael",
                          "lastName": "Stone",
                          "specialization": "Yoga",
                          "active": true
                        }
            """)
        .when()
        .put("/v1/trainers/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void changePasswordShouldReturnOkForTrainerToken() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINER);
    ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, PASSWORD, "new-password");
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Mike.Stone",
                          "oldPassword": "password",
                          "newPassword": "new-password"
                        }
            """)
        .when()
        .put("/v1/trainers/password")
        .then()
        .statusCode(200);

    verify(gymFacade).changeTrainerPassword(request);
    verify(fakeTokenService).updatePassword(TOKEN, "new-password");
  }

  @Test
  void changePasswordShouldRejectTraineeToken() {
    AuthenticatedUser user = new AuthenticatedUser("John.Doe", PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "John.Doe",
                          "oldPassword": "password",
                          "newPassword": "new-password"
                        }
            """)
        .when()
        .put("/v1/trainers/password")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void changePasswordShouldRejectAnotherUsername() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINER);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Another.User",
                          "oldPassword": "password",
                          "newPassword": "new-password"
                        }
            """)
        .when()
        .put("/v1/trainers/password")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldReturnOkForTrainerToken() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINER);
    AuthRequest request = new AuthRequest(USERNAME, PASSWORD);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Mike.Stone",
                          "active": false
                        }
            """)
        .when()
        .patch("/v1/trainers/profile/status")
        .then()
        .statusCode(200);

    verify(fakeTokenService).getUserByToken(TOKEN);
    verify(gymFacade).switchTrainerActiveStatus(request);
  }

  @Test
  void switchActiveStatusShouldRejectTraineeToken() {
    AuthenticatedUser user = new AuthenticatedUser("John.Doe", PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "John.Doe",
                          "active": false
                        }
            """)
        .when()
        .patch("/v1/trainers/profile/status")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldRejectAnotherUsername() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINER);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Another.User",
                          "active": false
                        }
            """)
        .when()
        .patch("/v1/trainers/profile/status")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldRejectInvalidToken() {
    when(fakeTokenService.getUserByToken("invalid-token"))
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", "invalid-token")
        .body(
            """
                        {
                          "username": "Mike.Stone",
                          "active": false
                        }
            """)
        .when()
        .patch("/v1/trainers/profile/status")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldReturnBadRequestWhenActiveIsMissing() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINER);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Mike.Stone"
                        }
            """)
        .when()
        .patch("/v1/trainers/profile/status")
        .then()
        .statusCode(400)
        .body("message", equalTo("Active status must not be null"));

    verifyNoInteractions(gymFacade);
  }
}
