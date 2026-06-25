package com.epam.gymcrm.web.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.workload.TrainerWorkloadMonthResponse;
import com.epam.gymcrm.dto.workload.TrainerWorkloadResponse;
import com.epam.gymcrm.dto.workload.TrainerWorkloadYearResponse;
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
class TrainerWorkloadControllerTest {

  @Mock private GymFacade gymFacade;

  @Mock private AuthenticatedUserProvider authenticatedUserProvider;

  @BeforeEach
  void setUp() {
    standaloneSetup(
        new TrainerWorkloadController(gymFacade, authenticatedUserProvider),
        new RestExceptionHandler());
  }

  @AfterEach
  void tearDown() {
    reset();
  }

  @Test
  void getTrainerWorkloadShouldReturnSummaryForAuthenticatedUser() {
    TrainerWorkloadResponse response =
        new TrainerWorkloadResponse(
            "Coach.Stone",
            "Coach",
            "Stone",
            true,
            List.of(new TrainerWorkloadYearResponse(
                2026,
                List.of(new TrainerWorkloadMonthResponse(5, 60)))));
    when(authenticatedUserProvider.currentUser())
        .thenReturn(new AuthenticatedPrincipal("John.Doe", ProfileType.TRAINEE));
    when(gymFacade.getTrainerWorkload("Coach.Stone")).thenReturn(response);

    given()
        .when()
        .get("/v1/trainer-workloads/Coach.Stone")
        .then()
        .statusCode(200)
        .body("trainerUsername", equalTo("Coach.Stone"))
        .body("trainerFirstName", equalTo("Coach"))
        .body("trainerLastName", equalTo("Stone"))
        .body("trainerStatus", equalTo(true))
        .body("years[0].year", equalTo(2026))
        .body("years[0].months[0].month", equalTo(5))
        .body("years[0].months[0].trainingSummaryDuration", equalTo(60));

    verify(authenticatedUserProvider).currentUser();
    verify(gymFacade).getTrainerWorkload("Coach.Stone");
  }

  @Test
  void getTrainerWorkloadShouldRejectInvalidToken() {
    when(authenticatedUserProvider.currentUser())
        .thenThrow(new AuthenticationException("Invalid authentication token"));

    given()
        .when()
        .get("/v1/trainer-workloads/Coach.Stone")
        .then()
        .statusCode(401)
        .body("message", equalTo("Invalid authentication token"));

    verifyNoInteractions(gymFacade);
  }
}
