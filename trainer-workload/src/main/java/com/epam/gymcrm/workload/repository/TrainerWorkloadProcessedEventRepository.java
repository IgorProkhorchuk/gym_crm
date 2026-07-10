package com.epam.gymcrm.workload.repository;

import com.epam.gymcrm.workload.model.TrainerWorkloadProcessedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for processed trainer workload events.
 */
public interface TrainerWorkloadProcessedEventRepository
    extends MongoRepository<TrainerWorkloadProcessedEvent, String> {
}
