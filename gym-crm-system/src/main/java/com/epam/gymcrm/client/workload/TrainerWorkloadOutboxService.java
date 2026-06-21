package com.epam.gymcrm.client.workload;

/**
 * Stores and dispatches trainer workload outbox events.
 */
public interface TrainerWorkloadOutboxService {

  /**
   * Stores pending trainer workload update event.
   *
   * @param trainingId source training id
   * @param request trainer workload update request
   */
  void savePendingEvent(Long trainingId, TrainerWorkloadRequest request);

  /**
   * Dispatches pending trainer workload update events.
   */
  void dispatchPendingEvents();
}
