package com.epam.gymcrm.web.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.training.TrainingTypeResponse;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.AuthenticatedPrincipal;
import com.epam.gymcrm.web.auth.AuthenticatedUserProvider;
import com.epam.gymcrm.web.exception.RestExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

  private static final String TOKEN = "token";

  @Mock private GymFacade gymFacade;

  @Mock private AuthenticatedUserProvider authenticatedUserProvider;

  @BeforeEach
  void setUp() {
    standaloneSetup(
        new TrainingTypeController(gymFacade, authenticatedUserProvider), new RestExceptionHandler());
  }

  @AfterEach
  void tearDown() {
    reset();
  }

  @Test
  void getTrainingTypesShouldReturnReferenceDataForTraineeToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal("John.Doe", ProfileType.TRAINEE);
    List<TrainingTypeResponse> response =
        List.of(new TrainingTypeResponse(1L, "Fitness"), new TrainingTypeResponse(2L, "Yoga"));
    when(authenticatedUserProvider.currentUser()).thenReturn(user);
    when(gymFacade.getTrainingTypes()).thenReturn(response);

    given()        .when()
        .get("/v1/training-types")
        .then()
        .statusCode(200)
        .body("size()", equalTo(2))
        .body("[0].id", equalTo(1))
        .body("[0].trainingTypeName", equalTo("Fitness"))
        .body("[1].id", equalTo(2))
        .body("[1].trainingTypeName", equalTo("Yoga"));

    verify(authenticatedUserProvider).currentUser();
    verify(gymFacade).getTrainingTypes();
  }

  @Test
  void getTrainingTypesShouldReturnReferenceDataForTrainerToken() {
    AuthenticatedPrincipal user = new AuthenticatedPrincipal("Mike.Stone", ProfileType.TRAINER);
    List<TrainingTypeResponse> response = List.of(new TrainingTypeResponse(1L, "Fitness"));
    when(authenticatedUserProvider.currentUser()).thenReturn(user);
    when(gymFacade.getTrainingTypes()).thenReturn(response);

    given()        .when()
        .get("/v1/training-types")
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].trainingTypeName", equalTo("Fitness"));

    verify(authenticatedUserProvider).currentUser();
    verify(gymFacade).getTrainingTypes();
  }

  @Test
  void getTrainingTypesShouldRejectInvalidToken() {
    when(authenticatedUserProvider.currentUser())
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()        .when()
        .get("/v1/training-types")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }
}
