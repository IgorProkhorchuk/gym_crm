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
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.AuthenticatedPrincipal;
import com.epam.gymcrm.web.auth.AuthenticatedUserProvider;
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
class TrainerControllerTest {

  private static final String TOKEN = "token";
  private static final String USERNAME = "Mike.Stone";
  private static final String PASSWORD = "password";

  @Mock private GymFacade gymFacade;

  @Mock private AuthenticatedUserProvider authenticatedUserProvider;

  @BeforeEach
  void setUp() {
    standaloneSetup(new TrainerController(gymFacade, authenticatedUserProvider), new RestExceptionHandler());
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
        .body("message", equalTo("Invalid request body: field 'firstName' must not be blank"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getTrainerProfileShouldReturnProfileForTrainerToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    AuthRequest request = new AuthRequest(USERNAME);
    TrainerProfileResponse response =
        new TrainerProfileResponse(USERNAME, "Mike", "Stone", true, "Fitness");
    when(authenticatedUserProvider.currentUser()).thenReturn(user);
    when(gymFacade.getTrainerProfile(request)).thenReturn(response);

    given()        .queryParam("username", USERNAME)
        .when()
        .get("/v1/trainers/profile")
        .then()
        .statusCode(200)
        .body("username", equalTo(USERNAME))
        .body("firstName", equalTo("Mike"))
        .body("lastName", equalTo("Stone"))
        .body("active", equalTo(true))
        .body("specialization", equalTo("Fitness"));

    verify(authenticatedUserProvider).currentUser();
    verify(gymFacade).getTrainerProfile(request);
  }

  @Test
  void getTrainerProfileShouldRejectTraineeToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal("John.Doe", ProfileType.TRAINEE);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()        .queryParam("username", "John.Doe")
        .when()
        .get("/v1/trainers/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("This operation is available only for trainer profiles"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getTrainerProfileShouldRejectAnotherUsername() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()        .queryParam("username", "Another.User")
        .when()
        .get("/v1/trainers/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Authenticated trainer can perform this operation only for own profile"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getTrainerProfileShouldRejectInvalidToken() {
    when(authenticatedUserProvider.currentUser())
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()        .queryParam("username", USERNAME)
        .when()
        .get("/v1/trainers/profile")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTrainerProfileShouldReturnUpdatedProfile() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    UpdateTrainerRequest request =
        new UpdateTrainerRequest(USERNAME, "Michael", "Stone", "Yoga", false);
    TrainerProfileResponse response =
        new TrainerProfileResponse(USERNAME, "Michael", "Stone", false, "Yoga");
    when(authenticatedUserProvider.currentUser()).thenReturn(user);
    when(gymFacade.updateTrainer(request)).thenReturn(response);

    given()
        .contentType(ContentType.JSON)        .body(
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

    verify(authenticatedUserProvider).currentUser();
    verify(gymFacade).updateTrainer(request);
  }

  @Test
  void updateTrainerProfileShouldRejectTraineeToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal("John.Doe", ProfileType.TRAINEE);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)        .body(
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
        .body("message", equalTo("This operation is available only for trainer profiles"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void updateTrainerProfileShouldRejectAnotherUsername() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)        .body(
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
        .body("message", equalTo("Authenticated trainer can perform this operation only for own profile"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void changePasswordShouldReturnOkForTrainerToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, PASSWORD, "new-password");
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)        .body(
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

    verify(gymFacade).changeTrainerPassword(request);  }

  @Test
  void changePasswordShouldRejectTraineeToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal("John.Doe", ProfileType.TRAINEE);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)        .body(
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
        .body("message", equalTo("This operation is available only for trainer profiles"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void changePasswordShouldRejectAnotherUsername() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)        .body(
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
        .body("message", equalTo("Authenticated trainer can perform this operation only for own profile"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldReturnOkForTrainerToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    AuthRequest request = new AuthRequest(USERNAME);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)        .body(
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

    verify(authenticatedUserProvider).currentUser();
    verify(gymFacade).switchTrainerActiveStatus(request);
  }

  @Test
  void switchActiveStatusShouldRejectTraineeToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal("John.Doe", ProfileType.TRAINEE);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)        .body(
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
        .body("message", equalTo("This operation is available only for trainer profiles"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldRejectAnotherUsername() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)        .body(
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
        .body("message", equalTo("Authenticated trainer can perform this operation only for own profile"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void switchActiveStatusShouldRejectInvalidToken() {
    when(authenticatedUserProvider.currentUser())
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .contentType(ContentType.JSON)        .body(
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
    given()
        .contentType(ContentType.JSON)        .body(
            """
                        {
                          "username": "Mike.Stone"
                        }
            """)
        .when()
        .patch("/v1/trainers/profile/status")
        .then()
        .statusCode(400)
        .body("message", equalTo("Invalid request body: field 'active' must not be null"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getTrainerTrainingsShouldReturnFilteredTrainingsForTrainerToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    TrainerTrainingsRequest request =
        new TrainerTrainingsRequest(
            USERNAME,
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28),
            "John Doe",
            PageRequest.firstPage());
    List<TrainerTrainingResponse> response =
        List.of(
            new TrainerTrainingResponse(
                "Evening Training", "Fitness", LocalDate.of(2026, 2, 15), 45, "John Doe"));
    when(authenticatedUserProvider.currentUser()).thenReturn(user);
    when(gymFacade.getTrainerTrainings(request)).thenReturn(response);

    given()        .queryParam("username", USERNAME)
        .queryParam("fromDate", "2026-02-01")
        .queryParam("toDate", "2026-02-28")
        .queryParam("traineeName", "John Doe")
        .when()
        .get("/v1/trainers/trainings")
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].trainingName", equalTo("Evening Training"))
        .body("[0].trainingType", equalTo("Fitness"))
        .body("[0].trainingDate", equalTo("2026-02-15"))
        .body("[0].trainingDuration", equalTo(45))
        .body("[0].traineeName", equalTo("John Doe"));

    verify(authenticatedUserProvider).currentUser();
    verify(gymFacade).getTrainerTrainings(request);
  }

  @Test
  void getTrainerTrainingsShouldRejectTraineeToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal("John.Doe", ProfileType.TRAINEE);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()        .queryParam("username", "John.Doe")
        .when()
        .get("/v1/trainers/trainings")
        .then()
        .statusCode(401)
        .body("message", equalTo("This operation is available only for trainer profiles"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getTrainerTrainingsShouldRejectAnotherUsername() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(USERNAME, ProfileType.TRAINER);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()        .queryParam("username", "Another.User")
        .when()
        .get("/v1/trainers/trainings")
        .then()
        .statusCode(401)
        .body("message", equalTo("Authenticated trainer can perform this operation only for own profile"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void getTrainerTrainingsShouldRejectInvalidToken() {
    when(authenticatedUserProvider.currentUser())
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()        .queryParam("username", USERNAME)
        .when()
        .get("/v1/trainers/trainings")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }
}
