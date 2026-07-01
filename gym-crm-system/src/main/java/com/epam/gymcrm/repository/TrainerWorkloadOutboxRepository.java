package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.TrainerWorkloadOutboxEvent;
import com.epam.gymcrm.model.TrainerWorkloadOutboxStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository contract for trainer workload outbox events. */
public interface TrainerWorkloadOutboxRepository
    extends JpaRepository<TrainerWorkloadOutboxEvent, Long> {

  /**
   * Finds due outbox events by status ordered by creation time.
   *
   * @param status event status to dispatch
   * @param nextRetryAt maximum next retry time
   * @param pageable batch limit
   * @return due outbox events
   */
  List<TrainerWorkloadOutboxEvent> findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
      TrainerWorkloadOutboxStatus status, Instant nextRetryAt, Pageable pageable);
}
