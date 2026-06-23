package com.epam.gymcrm.client.workload;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * REST implementation of trainer workload service client.
 */
@Component
@RequiredArgsConstructor
public class TrainerWorkloadRestClient implements TrainerWorkloadClient {

  private static final String TRAINER_WORKLOAD_URL =
      "http://trainer-workload/api/v1/trainer-workloads";

  private final RestTemplate restTemplate;

  @Override
  public void updateTrainerWorkload(TrainerWorkloadRequest request) {
    restTemplate.postForEntity(TRAINER_WORKLOAD_URL, request, Void.class);
  }
}
