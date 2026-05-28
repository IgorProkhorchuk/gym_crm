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
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.FakeTokenService;
import com.epam.gymcrm.web.exception.RestExceptionHandler;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

  private static final String TOKEN = "token";
  private static final String USERNAME = "John.Doe";
  private static final String PASSWORD = "password";

  @Mock private GymFacade gymFacade;

  @Mock private FakeTokenService fakeTokenService;

  @BeforeEach
  void setUp() {
    standaloneSetup(new TraineeController(gymFacade, fakeTokenService), new RestExceptionHandler());
  }

  @AfterEach
  void tearDown() {
    reset();
  }

  @Test
  void createTraineeShouldReturnCreatedCredentials() {
    CreateTraineeRequest request =
        new CreateTraineeRequest("John", "Doe", LocalDate.of(1995, 1, 10), "Main Street, 123");
    UsernamePasswordResponse response = new UsernamePasswordResponse(USERNAME, PASSWORD);
    when(gymFacade.createTrainee(request)).thenReturn(response);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                        {
                          "firstName": "John",
                          "lastName": "Doe",
                          "dateOfBirth": "1995-01-10",
                          "address": "Main Street, 123"
                        }
            """)
        .when()
        .post("/v1/trainees")
        .then()
        .statusCode(201)
        .body("username", equalTo(USERNAME))
        .body("password", equalTo(PASSWORD));

    verify(gymFacade).createTrainee(request);
  }

  @Test
  void createTraineeShouldReturnBadRequestWhenRequestIsInvalid() {
    CreateTraineeRequest request = new CreateTraineeRequest("", "Doe", null, null);
    when(gymFacade.createTrainee(request))
        .thenThrow(new IllegalArgumentException("First name must not be blank"));

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                        {
                          "firstName": "",
                          "lastName": "Doe"
                        }
            """)
        .when()
        .post("/v1/trainees")
        .then()
        .statusCode(400)
        .body("message", equalTo("First name must not be blank"));

    verify(gymFacade).createTrainee(request);
  }

  @Test
  void getTraineeProfileShouldReturnProfileForTraineeToken() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    AuthRequest request = new AuthRequest(USERNAME, PASSWORD);
    TraineeProfileResponse response =
        new TraineeProfileResponse(
            USERNAME,
            "John",
            "Doe",
            true,
            LocalDate.of(1995, 1, 10),
            "Main Street, 123",
            List.of());
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);
    when(gymFacade.getTraineeProfile(request)).thenReturn(response);

    given()
        .header("X-Auth-Token", TOKEN)
        .when()
        .get("/v1/trainees/profile")
        .then()
        .statusCode(200)
        .body("username", equalTo(USERNAME))
        .body("firstName", equalTo("John"))
        .body("lastName", equalTo("Doe"))
        .body("active", equalTo(true))
        .body("address", equalTo("Main Street, 123"));

    verify(fakeTokenService).getUserByToken(TOKEN);
    verify(gymFacade).getTraineeProfile(request);
  }

  @Test
  void getTraineeProfileShouldRejectTrainerToken() {
    AuthenticatedUser user = new AuthenticatedUser("Mike.Stone", PASSWORD, ProfileType.TRAINER);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .header("X-Auth-Token", TOKEN)
        .when()
        .get("/v1/trainees/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getTraineeProfileShouldRejectInvalidToken() {
    when(fakeTokenService.getUserByToken("invalid-token"))
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .header("X-Auth-Token", "invalid-token")
        .when()
        .get("/v1/trainees/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTraineeProfileShouldReturnUpdatedProfile() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    UpdateTraineeRequest request =
        new UpdateTraineeRequest(
            USERNAME,
            PASSWORD,
            "Johnny",
            "Doe",
            LocalDate.of(1996, 2, 20),
            "Updated Street, 7",
            false);
    TraineeProfileResponse response =
        new TraineeProfileResponse(
            USERNAME,
            "Johnny",
            "Doe",
            false,
            LocalDate.of(1996, 2, 20),
            "Updated Street, 7",
            List.of());
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);
    when(gymFacade.updateTrainee(request)).thenReturn(response);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "John.Doe",
                          "firstName": "Johnny",
                          "lastName": "Doe",
                          "dateOfBirth": "1996-02-20",
                          "address": "Updated Street, 7",
                          "active": false
                        }
            """)
        .when()
        .put("/v1/trainees/profile")
        .then()
        .statusCode(200)
        .body("username", equalTo(USERNAME))
        .body("firstName", equalTo("Johnny"))
        .body("lastName", equalTo("Doe"))
        .body("active", equalTo(false))
        .body("dateOfBirth", equalTo(List.of(1996, 2, 20)))
        .body("address", equalTo("Updated Street, 7"));

    verify(fakeTokenService).getUserByToken(TOKEN);
    verify(gymFacade).updateTrainee(request);
  }

  @Test
  void updateTraineeProfileShouldRejectTrainerToken() {
    AuthenticatedUser user = new AuthenticatedUser("Mike.Stone", PASSWORD, ProfileType.TRAINER);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Mike.Stone",
                          "firstName": "Mike",
                          "lastName": "Stone",
                          "dateOfBirth": "1996-02-20",
                          "address": "Updated Street, 7",
                          "active": true
                        }
            """)
        .when()
        .put("/v1/trainees/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTraineeProfileShouldRejectAnotherUsername() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Another.User",
                          "firstName": "Johnny",
                          "lastName": "Doe",
                          "dateOfBirth": "1996-02-20",
                          "address": "Updated Street, 7",
                          "active": true
                        }
            """)
        .when()
        .put("/v1/trainees/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void changePasswordShouldReturnOkForTraineeToken() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, PASSWORD, "new-password");
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
        .put("/v1/trainees/password")
        .then()
        .statusCode(200);

    verify(gymFacade).changeTraineePassword(request);
    verify(fakeTokenService).updatePassword(TOKEN, "new-password");
  }

  @Test
  void changePasswordShouldRejectTrainerToken() {
    AuthenticatedUser user = new AuthenticatedUser("Mike.Stone", PASSWORD, ProfileType.TRAINER);
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
        .put("/v1/trainees/password")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void changePasswordShouldRejectAnotherUsername() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
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
        .put("/v1/trainees/password")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldReturnOkForTraineeToken() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    AuthRequest request = new AuthRequest(USERNAME, PASSWORD);
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
        .patch("/v1/trainees/profile/status")
        .then()
        .statusCode(200);

    verify(fakeTokenService).getUserByToken(TOKEN);
    verify(gymFacade).switchTraineeActiveStatus(request);
  }

  @Test
  void switchActiveStatusShouldRejectTrainerToken() {
    AuthenticatedUser user = new AuthenticatedUser("Mike.Stone", PASSWORD, ProfileType.TRAINER);
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
        .patch("/v1/trainees/profile/status")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldRejectAnotherUsername() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
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
        .patch("/v1/trainees/profile/status")
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
                          "username": "John.Doe",
                          "active": false
                        }
            """)
        .when()
        .patch("/v1/trainees/profile/status")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldReturnBadRequestWhenActiveIsMissing() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "John.Doe"
                        }
            """)
        .when()
        .patch("/v1/trainees/profile/status")
        .then()
        .statusCode(400)
        .body("message", equalTo("Active status must not be null"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void deleteTraineeProfileShouldReturnOk() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    AuthRequest request = new AuthRequest(USERNAME, PASSWORD);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "John.Doe"
                        }
            """)
        .when()
        .delete("/v1/trainees/profile")
        .then()
        .statusCode(200);

    verify(fakeTokenService).getUserByToken(TOKEN);
    verify(gymFacade).deleteTraineeByUsername(request);
  }

  @Test
  void deleteTraineeProfileShouldRejectTrainerToken() {
    AuthenticatedUser user =
        new AuthenticatedUser("Petryk.Pjatochkyn", PASSWORD, ProfileType.TRAINER);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Petryk.Pjatochkyn"
                        }
            """)
        .when()
        .delete("/v1/trainees/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void deleteTraineeProfileShouldRejectAnotherUsername() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "username": "Another.User"
                        }
            """)
        .when()
        .delete("/v1/trainees/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void deleteTraineeProfileShouldRejectInvalidToken() {
    when(fakeTokenService.getUserByToken("invalid-token"))
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", "invalid-token")
        .body(
            """
                        {
                          "username": "John.Doe"
                        }
            """)
        .when()
        .delete("/v1/trainees/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getUnassignedTrainersShouldReturnTrainersForTraineeToken() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    AuthRequest request = new AuthRequest(USERNAME, PASSWORD);
    List<TrainerSummaryResponse> response =
        List.of(
            new TrainerSummaryResponse("Mike.Stone", "Mike", "Stone", "Fitness"),
            new TrainerSummaryResponse("Kate.Yoga", "Kate", "Yoga", "Yoga"));
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);
    when(gymFacade.getUnassignedTrainers(request)).thenReturn(response);

    given()
        .header("X-Auth-Token", TOKEN)
        .when()
        .get("/v1/trainees/trainers/unassigned")
        .then()
        .statusCode(200)
        .body("size()", equalTo(2))
        .body("[0].username", equalTo("Mike.Stone"))
        .body("[0].firstName", equalTo("Mike"))
        .body("[0].lastName", equalTo("Stone"))
        .body("[0].specialization", equalTo("Fitness"))
        .body("[1].username", equalTo("Kate.Yoga"))
        .body("[1].firstName", equalTo("Kate"))
        .body("[1].lastName", equalTo("Yoga"))
        .body("[1].specialization", equalTo("Yoga"));

    verify(fakeTokenService).getUserByToken(TOKEN);
    verify(gymFacade).getUnassignedTrainers(request);
  }

  @Test
  void getUnassignedTrainersShouldRejectTrainerToken() {
    AuthenticatedUser user = new AuthenticatedUser("Mike.Stone", PASSWORD, ProfileType.TRAINER);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .header("X-Auth-Token", TOKEN)
        .when()
        .get("/v1/trainees/trainers/unassigned")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getUnassignedTrainersShouldRejectInvalidToken() {
    when(fakeTokenService.getUserByToken("invalid-token"))
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .header("X-Auth-Token", "invalid-token")
        .when()
        .get("/v1/trainees/trainers/unassigned")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTraineeTrainersShouldReturnUpdatedTrainerList() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest(USERNAME, PASSWORD, List.of("Mike.Stone", "Kate.Yoga"));
    List<TrainerSummaryResponse> response =
        List.of(
            new TrainerSummaryResponse("Mike.Stone", "Mike", "Stone", "Fitness"),
            new TrainerSummaryResponse("Kate.Yoga", "Kate", "Yoga", "Yoga"));
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);
    when(gymFacade.updateTraineeTrainers(request)).thenReturn(response);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "traineeUsername": "John.Doe",
                          "trainerUsernames": [
                            "Mike.Stone",
                            "Kate.Yoga"
                          ]
                        }
            """)
        .when()
        .put("/v1/trainees/trainers")
        .then()
        .statusCode(200)
        .body("size()", equalTo(2))
        .body("[0].username", equalTo("Mike.Stone"))
        .body("[0].firstName", equalTo("Mike"))
        .body("[0].lastName", equalTo("Stone"))
        .body("[0].specialization", equalTo("Fitness"))
        .body("[1].username", equalTo("Kate.Yoga"))
        .body("[1].firstName", equalTo("Kate"))
        .body("[1].lastName", equalTo("Yoga"))
        .body("[1].specialization", equalTo("Yoga"));

    verify(fakeTokenService).getUserByToken(TOKEN);
    verify(gymFacade).updateTraineeTrainers(request);
  }

  @Test
  void updateTraineeTrainersShouldRejectTrainerToken() {
    AuthenticatedUser user = new AuthenticatedUser("Mike.Stone", PASSWORD, ProfileType.TRAINER);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "traineeUsername": "Mike.Stone",
                          "trainerUsernames": [
                            "Kate.Yoga"
                          ]
                        }
            """)
        .when()
        .put("/v1/trainees/trainers")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTraineeTrainersShouldRejectAnotherUsername() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "traineeUsername": "Another.User",
                          "trainerUsernames": [
                            "Mike.Stone"
                          ]
                        }
            """)
        .when()
        .put("/v1/trainees/trainers")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTraineeTrainersShouldRejectInvalidToken() {
    when(fakeTokenService.getUserByToken("invalid-token"))
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", "invalid-token")
        .body(
            """
                        {
                          "traineeUsername": "John.Doe",
                          "trainerUsernames": [
                            "Mike.Stone"
                          ]
                        }
            """)
        .when()
        .put("/v1/trainees/trainers")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTraineeTrainersShouldReturnBadRequestWhenTrainerUsernamesAreMissing() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "traineeUsername": "John.Doe"
                        }
            """)
        .when()
        .put("/v1/trainees/trainers")
        .then()
        .statusCode(400)
        .body("message", equalTo("Trainer usernames must not be null"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTraineeTrainersShouldReturnBadRequestWhenTrainerUsernameIsBlank() {
    AuthenticatedUser user = new AuthenticatedUser(USERNAME, PASSWORD, ProfileType.TRAINEE);
    when(fakeTokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(
            """
                        {
                          "traineeUsername": "John.Doe",
                          "trainerUsernames": [
                            ""
                          ]
                        }
            """)
        .when()
        .put("/v1/trainees/trainers")
        .then()
        .statusCode(400)
        .body("message", equalTo("Trainer username must not be blank"));

    verifyNoInteractions(gymFacade);
  }
}
