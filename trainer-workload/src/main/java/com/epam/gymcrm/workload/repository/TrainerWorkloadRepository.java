package com.epam.gymcrm.workload.repository;

import com.epam.gymcrm.workload.model.TrainerWorkload;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for trainer workload documents.
 */
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

  /**
   * Finds trainer workload document by trainer username.
   *
   * @param username trainer username
   * @return trainer workload document if it exists
   */
  Optional<TrainerWorkload> findByUsername(String username);
}
