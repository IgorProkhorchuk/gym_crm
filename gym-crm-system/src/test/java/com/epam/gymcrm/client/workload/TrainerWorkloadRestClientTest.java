package com.epam.gymcrm.client.workload;

import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadRestClientTest {

  @InjectMocks private TrainerWorkloadRestClient trainerWorkloadRestClient;

  @Mock private RestTemplate restTemplate;

  @Test
  void updateTrainerWorkloadShouldPostRequestToTrainerWorkloadService() {
    TrainerWorkloadRequest request =
        new TrainerWorkloadRequest(
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
}
