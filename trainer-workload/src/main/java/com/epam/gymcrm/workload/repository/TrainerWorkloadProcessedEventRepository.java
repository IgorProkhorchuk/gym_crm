package com.epam.gymcrm.workload.repository;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.model.TrainerWorkloadProcessedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for processed trainer workload events.
 */
public interface TrainerWorkloadProcessedEventRepository
    extends MongoRepository<TrainerWorkloadProcessedEvent, String> {

  /**
   * Checks whether a workload event has already been processed.
   *
   * @param trainingId training identifier
   * @param actionType workload action type
   * @return true when the event was already processed
   */
  boolean existsByTrainingIdAndActionType(Long trainingId, ActionType actionType);
}
