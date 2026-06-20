package com.epam.gymcrm.workload.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadMonthResponse;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.epam.gymcrm.workload.dto.TrainerWorkloadYearResponse;
import com.epam.gymcrm.workload.exception.RestExceptionHandler;
import com.epam.gymcrm.workload.exception.TrainerWorkloadNotFoundException;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
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
class TrainerWorkloadControllerTest {

  @Mock private TrainerWorkloadService trainerWorkloadService;

  @BeforeEach
  void setUp() {
    standaloneSetup(new TrainerWorkloadController(trainerWorkloadService), new RestExceptionHandler());
  }

  @AfterEach
  void tearDown() {
    reset();
  }

  @Test
  void updateTrainerWorkloadShouldReturnOk() {
    TrainerWorkloadRequest request =
        new TrainerWorkloadRequest(
            "John.Doe",
            "John",
            "Doe",
            true,
            LocalDate.of(2026, 6, 20),
            60,
            ActionType.ADD);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                {
                  "trainerUsername": "John.Doe",
                  "trainerFirstName": "John",
                  "trainerLastName": "Doe",
                  "trainerStatus": true,
                  "trainingDate": "2026-06-20",
                  "trainingDuration": 60,
                  "actionType": "ADD"
                }
            """)
        .when()
        .post("/v1/trainer-workloads")
        .then()
        .statusCode(200);

    verify(trainerWorkloadService).updateTrainerWorkload(request);
  }

  @Test
  void getTrainerWorkloadShouldReturnOk() {
    TrainerWorkloadResponse response =
        new TrainerWorkloadResponse(
            "John.Doe",
            "John",
            "Doe",
            true,
            List.of(
                new TrainerWorkloadYearResponse(
                    2026,
                    List.of(new TrainerWorkloadMonthResponse(6, 60)))));
    when(trainerWorkloadService.getTrainerWorkload("John.Doe")).thenReturn(response);

    given()
        .when()
        .get("/v1/trainer-workloads/John.Doe")
        .then()
        .statusCode(200)
        .body("trainerUsername", equalTo("John.Doe"))
        .body("trainerFirstName", equalTo("John"))
        .body("trainerLastName", equalTo("Doe"))
        .body("trainerStatus", equalTo(true))
        .body("years[0].year", equalTo(2026))
        .body("years[0].months[0].month", equalTo(6))
        .body("years[0].months[0].trainingSummaryDuration", equalTo(60));

    verify(trainerWorkloadService).getTrainerWorkload("John.Doe");
  }

  @Test
  void getTrainerWorkloadShouldReturnNotFoundWhenTrainerDoesNotExist() {
    when(trainerWorkloadService.getTrainerWorkload("Unknown.User"))
        .thenThrow(new TrainerWorkloadNotFoundException("Unknown.User"));

    given()
        .when()
        .get("/v1/trainer-workloads/Unknown.User")
        .then()
        .statusCode(404)
        .body("message", equalTo("Trainer workload not found: Unknown.User"));
  }

  @Test
  void updateTrainerWorkloadShouldReturnBadRequestWhenServiceRejectsRequest() {
    TrainerWorkloadRequest request =
        new TrainerWorkloadRequest(
            "John.Doe",
            "John",
            "Doe",
            true,
            LocalDate.of(2026, 6, 20),
            60,
            ActionType.DELETE);
    org.mockito.Mockito.doThrow(
            new IllegalArgumentException("Training summary duration cannot be negative"))
        .when(trainerWorkloadService)
        .updateTrainerWorkload(request);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
                {
                  "trainerUsername": "John.Doe",
                  "trainerFirstName": "John",
                  "trainerLastName": "Doe",
                  "trainerStatus": true,
                  "trainingDate": "2026-06-20",
                  "trainingDuration": 60,
                  "actionType": "DELETE"
                }
            """)
        .when()
        .post("/v1/trainer-workloads")
        .then()
        .statusCode(400)
        .body("message", equalTo("Training summary duration cannot be negative"));
  }
}
