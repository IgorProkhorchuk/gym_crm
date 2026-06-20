package com.epam.gymcrm.workload.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.reset;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.standaloneSetup;
import static org.mockito.Mockito.verify;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import io.restassured.http.ContentType;
import java.time.LocalDate;
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
    standaloneSetup(new TrainerWorkloadController(trainerWorkloadService));
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
}
