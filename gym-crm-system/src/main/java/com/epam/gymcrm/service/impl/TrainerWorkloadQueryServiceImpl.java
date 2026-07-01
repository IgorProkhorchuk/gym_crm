package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.client.workload.TrainerWorkloadClient;
import com.epam.gymcrm.dto.workload.TrainerWorkloadResponse;
import com.epam.gymcrm.service.TrainerWorkloadQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainerWorkloadQueryServiceImpl implements TrainerWorkloadQueryService {

  private final TrainerWorkloadClient trainerWorkloadClient;

  @Override
  public TrainerWorkloadResponse getTrainerWorkload(String username) {
    return trainerWorkloadClient.getTrainerWorkload(username);
  }
}
