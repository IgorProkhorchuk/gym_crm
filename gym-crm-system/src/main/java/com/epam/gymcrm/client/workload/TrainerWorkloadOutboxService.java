package com.epam.gymcrm.client.workload;

/**
 * Stores trainer workload updates for reliable retry and dispatches due events.
 */
public interface TrainerWorkloadOutboxService {

  /**
   * Stores a pending trainer workload update event in the outbox.
   *
   * @param trainingId source training id
   * @param request trainer workload update request
   */
  void savePendingEvent(Long trainingId, TrainerWorkloadRequest request);

  /**
   * Dispatches due pending trainer workload update events.
   */
  void dispatchPendingEvents();
}
