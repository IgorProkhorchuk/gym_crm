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
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.TokenService;
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

  @Mock private TokenService tokenService;

  @BeforeEach
  void setUp() {
    standaloneSetup(new TrainingController(gymFacade, tokenService), new RestExceptionHandler());
  }

  @AfterEach
  void tearDown() {
    reset();
  }

  @Test
  void addTrainingShouldReturnOkForTraineeToken() {
    AuthenticatedUser user =
        new AuthenticatedUser(TRAINEE_USERNAME, PASSWORD, ProfileType.TRAINEE);
    AddTrainingRequest request =
        new AddTrainingRequest(
            TRAINEE_USERNAME,
            PASSWORD,
            TRAINER_USERNAME,
            "Morning Training",
            "Fitness",
            LocalDate.of(2026, 1, 10),
            60);
    when(tokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
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

    verify(tokenService).getUserByToken(TOKEN);
    verify(gymFacade).addTraining(request);
  }

  @Test
  void addTrainingShouldRejectTrainerToken() {
    AuthenticatedUser user =
        new AuthenticatedUser(TRAINER_USERNAME, PASSWORD, ProfileType.TRAINER);
    when(tokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
        .body(validRequestBody())
        .when()
        .post("/v1/trainings")
        .then()
        .statusCode(401)
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void addTrainingShouldRejectAnotherUsername() {
    AuthenticatedUser user =
        new AuthenticatedUser(TRAINEE_USERNAME, PASSWORD, ProfileType.TRAINEE);
    when(tokenService.getUserByToken(TOKEN)).thenReturn(user);

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", TOKEN)
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
        .body("message", equalTo("Access denied"));

    verifyNoInteractions(gymFacade);
  }

  @Test
  void addTrainingShouldRejectInvalidToken() {
    when(tokenService.getUserByToken("invalid-token"))
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .contentType(ContentType.JSON)
        .header("X-Auth-Token", "invalid-token")
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
        .header("X-Auth-Token", TOKEN)
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
            "message",
            equalTo("Invalid request body: field 'trainingDuration' must be positive"));

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
