package com.epam.gymcrm.client.workload;

import com.epam.gymcrm.dto.workload.TrainerWorkloadResponse;
import com.epam.gymcrm.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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

  @Override
  public TrainerWorkloadResponse getTrainerWorkload(String username) {
    try {
      return restTemplate.getForObject(TRAINER_WORKLOAD_URL + "/{username}",
          TrainerWorkloadResponse.class,
          username);
    } catch (HttpClientErrorException.NotFound exception) {
      throw new EntityNotFoundException("Trainer workload not found: " + username);
    }
  }
}
