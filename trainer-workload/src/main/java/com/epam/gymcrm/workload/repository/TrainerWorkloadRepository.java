package com.epam.gymcrm.workload.repository;

import com.epam.gymcrm.workload.model.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for trainer workload records.
 */
public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, String> {
}
