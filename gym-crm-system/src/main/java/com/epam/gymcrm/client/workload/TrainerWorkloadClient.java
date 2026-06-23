package com.epam.gymcrm.client.workload;

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
}
