package com.epam.gymcrm.workload.service;

import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;

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
}
