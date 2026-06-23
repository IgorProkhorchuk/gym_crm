package com.epam.gymcrm.client.workload;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled dispatcher for trainer workload outbox events.
 */
@Component
@RequiredArgsConstructor
public class TrainerWorkloadOutboxDispatcher {

  private final TrainerWorkloadOutboxService trainerWorkloadOutboxService;

  /**
   * Dispatches pending trainer workload events on schedule.
   */
  @Scheduled(
      initialDelayString = "${trainer-workload.outbox.dispatcher.initial-delay:10000}",
      fixedDelayString = "${trainer-workload.outbox.dispatcher.fixed-delay:10000}")
  public void dispatchPendingEvents() {
    trainerWorkloadOutboxService.dispatchPendingEvents();
  }
}
