package com.epam.gymcrm.workload.service;

import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.dto.TrainerWorkloadResponse;

/**
 * Handles trainer workload updates received from the main Gym CRM service.
 */
public interface TrainerWorkloadService {

  /**
   * Updates trainer monthly workload summary according to the requested action.
   *
   * @param request trainer workload update request
   */
  void updateTrainerWorkload(TrainerWorkloadRequest request);

  /**
   * Returns trainer workload summary grouped by years and months.
   *
   * @param username trainer username
   * @return trainer workload summary
   */
  TrainerWorkloadResponse getTrainerWorkload(String username);
}
