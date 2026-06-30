package com.epam.gymcrm.client.workload;

/**
 * Sends trainer workload updates without failing the main training flow.
 */
public interface TrainerWorkloadNotificationService {

  /**
   * Sends trainer workload update and returns delivery result.
   *
   * @param request trainer workload update request
   * @return delivery result
   */
  TrainerWorkloadNotificationResult notifyTrainerWorkload(TrainerWorkloadRequest request);
}
