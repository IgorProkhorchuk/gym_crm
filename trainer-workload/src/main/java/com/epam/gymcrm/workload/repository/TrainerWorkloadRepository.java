package com.epam.gymcrm.workload.repository;

import com.epam.gymcrm.workload.model.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for trainer workload documents.
 */
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {
}
