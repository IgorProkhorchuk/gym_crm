package com.epam.gymcrm.workload.repository;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.model.TrainerWorkloadProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for trainer workload idempotency records.
 */
public interface TrainerWorkloadProcessedEventRepository
    extends JpaRepository<TrainerWorkloadProcessedEvent, Long> {

  /**
   * Checks whether a workload event was already applied.
   *
   * @param trainingId source training id
   * @param actionType workload update action
   * @return true when the event was already processed
   */
  boolean existsByTrainingIdAndActionType(Long trainingId, ActionType actionType);
}
