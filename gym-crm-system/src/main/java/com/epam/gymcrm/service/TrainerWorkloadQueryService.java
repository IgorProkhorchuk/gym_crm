package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.workload.TrainerWorkloadResponse;

/**
 * Provides trainer workload summaries through the internal workload service.
 */
public interface TrainerWorkloadQueryService {

  /**
   * Gets trainer workload summary.
   *
   * @param username trainer username
   * @return trainer workload summary
   */
  TrainerWorkloadResponse getTrainerWorkload(String username);
}
