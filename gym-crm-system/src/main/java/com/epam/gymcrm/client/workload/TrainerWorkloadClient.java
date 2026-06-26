package com.epam.gymcrm.client.workload;

import com.epam.gymcrm.dto.workload.TrainerWorkloadResponse;

/**
 * Client for trainer workload service integration.
 */
public interface TrainerWorkloadClient {

  /**
   * Sends trainer workload update request to workload service.
   *
   * @param request trainer workload update request
   */
  void updateTrainerWorkload(TrainerWorkloadRequest request);

  /**
   * Gets trainer workload summary from workload service.
   *
   * @param username trainer username
   * @return trainer workload summary
   */
  TrainerWorkloadResponse getTrainerWorkload(String username);
}
