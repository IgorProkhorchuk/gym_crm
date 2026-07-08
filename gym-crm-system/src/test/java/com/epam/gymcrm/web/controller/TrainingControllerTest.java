package com.epam.gymcrm.web.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.AuthenticatedPrincipal;
import com.epam.gymcrm.web.auth.AuthenticatedUserProvider;
import com.epam.gymcrm.web.exception.RestExceptionHandler;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

  private static final String TOKEN = "token";
  private static final String TRAINEE_USERNAME = "John.Doe";
  private static final String TRAINER_USERNAME = "Mike.Stone";
  private static final String PASSWORD = "password";

  @Mock private GymFacade gymFacade;

  @Mock private AuthenticatedUserProvider authenticatedUserProvider;

  @BeforeEach
  void setUp() {
    standaloneSetup(
        new TrainingController(gymFacade, authenticatedUserProvider), new RestExceptionHandler());
  }

  @AfterEach
  void tearDown() {
    reset();
  }

  @Test
  void addTrainingShouldReturnOkForTraineeToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(TRAINEE_USERNAME, ProfileType.TRAINEE);
    AddTrainingRequest request =
        new AddTrainingRequest(
            TRAINEE_USERNAME,
            TRAINER_USERNAME,
            "Morning Training",
            "Fitness",
            LocalDate.of(2026, 1, 10),
            60);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                        {
                          "traineeUsername": "John.Doe",
                          "trainerUsername": "Mike.Stone",
                          "trainingName": "Morning Training",
                          "trainingTypeName": "Fitness",
                          "trainingDate": "2026-01-10",
                          "trainingDuration": 60
                        }
            """)
        .when()
        .post("/v1/trainings")
        .then()
        .statusCode(200);

    verify(authenticatedUserProvider).currentUser();
    verify(gymFacade).addTraining(request);
  }

  @Test
  void addTrainingShouldRejectTrainerToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(TRAINER_USERNAME, ProfileType.TRAINER);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .body(validRequestBody())
        .when()
        .post("/v1/trainings")
        .then()
        .statusCode(401)
        .body("message", equalTo("This operation is available only for trainee profiles"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void addTrainingShouldRejectAnotherUsername() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(TRAINEE_USERNAME, ProfileType.TRAINEE);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                        {
                          "traineeUsername": "Another.User",
                          "trainerUsername": "Mike.Stone",
                          "trainingName": "Morning Training",
                          "trainingTypeName": "Fitness",
                          "trainingDate": "2026-01-10",
                          "trainingDuration": 60
                        }
            """)
        .when()
        .post("/v1/trainings")
        .then()
        .statusCode(401)
        .body("message", equalTo("Authenticated trainee can add trainings only for own profile"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void addTrainingShouldRejectInvalidToken() {
    when(authenticatedUserProvider.currentUser())
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .contentType(ContentType.JSON)
        .body(validRequestBody())
        .when()
        .post("/v1/trainings")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void addTrainingShouldReturnBadRequestWhenDurationIsNotPositive() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
                        {
                          "traineeUsername": "John.Doe",
                          "trainerUsername": "Mike.Stone",
                          "trainingName": "Morning Training",
                          "trainingTypeName": "Fitness",
                          "trainingDate": "2026-01-10",
                          "trainingDuration": 0
                        }
            """)
        .when()
        .post("/v1/trainings")
        .then()
        .statusCode(400)
        .body(
            "message", equalTo("Invalid request body: field 'trainingDuration' must be positive"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void deleteTrainingShouldReturnNoContent() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal(TRAINEE_USERNAME, ProfileType.TRAINEE);
    when(authenticatedUserProvider.currentUser()).thenReturn(user);

    given().when().delete("/v1/trainings/10").then().statusCode(204);

    verify(authenticatedUserProvider).currentUser();
    verify(gymFacade).deleteTraining(10L);
  }

  @Test
  void deleteTrainingShouldRejectInvalidToken() {
    when(authenticatedUserProvider.currentUser())
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .when()
        .delete("/v1/trainings/10")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }

  private static String validRequestBody() {
    return
        """
                {
                  "traineeUsername": "John.Doe",
                  "trainerUsername": "Mike.Stone",
                  "trainingName": "Morning Training",
                  "trainingTypeName": "Fitness",
                  "trainingDate": "2026-01-10",
                  "trainingDuration": 60
                }
        """;
  }
}
