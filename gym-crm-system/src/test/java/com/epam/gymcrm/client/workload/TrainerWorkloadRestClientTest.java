package com.epam.gymcrm.client.workload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.epam.gymcrm.dto.workload.TrainerWorkloadMonthResponse;
import com.epam.gymcrm.dto.workload.TrainerWorkloadResponse;
import com.epam.gymcrm.dto.workload.TrainerWorkloadYearResponse;
import com.epam.gymcrm.exception.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadRestClientTest {

  @InjectMocks private TrainerWorkloadRestClient trainerWorkloadRestClient;

  @Mock private RestTemplate restTemplate;

  @Test
  void updateTrainerWorkloadShouldPostRequestToTrainerWorkloadService() {
    TrainerWorkloadRequest request =
        new TrainerWorkloadRequest(
            10L,
            "Coach.Stone",
            "Coach",
            "Stone",
            true,
            LocalDate.of(2026, 5, 3),
            60,
            TrainerWorkloadActionType.ADD);

    trainerWorkloadRestClient.updateTrainerWorkload(request);

    verify(restTemplate)
        .postForEntity(
            "http://trainer-workload/api/v1/trainer-workloads",
            request,
            Void.class);
  }

  @Test
  void getTrainerWorkloadShouldReturnResponseFromTrainerWorkloadService() {
    TrainerWorkloadResponse response =
        new TrainerWorkloadResponse(
            "Coach.Stone",
            "Coach",
            "Stone",
            true,
            List.of(new TrainerWorkloadYearResponse(
                2026,
                List.of(new TrainerWorkloadMonthResponse(5, 60)))));
    org.mockito.Mockito.when(restTemplate.getForObject(
            "http://trainer-workload/api/v1/trainer-workloads/{username}",
            TrainerWorkloadResponse.class,
            "Coach.Stone"))
        .thenReturn(response);

    TrainerWorkloadResponse result = trainerWorkloadRestClient.getTrainerWorkload("Coach.Stone");

    assertThat(result).isEqualTo(response);
  }

  @Test
  void getTrainerWorkloadShouldThrowEntityNotFoundExceptionWhenWorkloadServiceReturnsNotFound() {
    org.mockito.Mockito.when(restTemplate.getForObject(
            "http://trainer-workload/api/v1/trainer-workloads/{username}",
            TrainerWorkloadResponse.class,
            "Unknown.User"))
        .thenThrow(HttpClientErrorException.create(
            HttpStatus.NOT_FOUND,
            "Not Found",
            org.springframework.http.HttpHeaders.EMPTY,
            new byte[0],
            null));

    assertThatThrownBy(() -> trainerWorkloadRestClient.getTrainerWorkload("Unknown.User"))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Trainer workload not found: Unknown.User");
  }
}
