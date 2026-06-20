package com.epam.gymcrm.workload.repository;

import com.epam.gymcrm.workload.model.TrainerMonthlySummary;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for trainer monthly workload summaries.
 */
public interface TrainerMonthlySummaryRepository extends JpaRepository<TrainerMonthlySummary, Long> {

  /**
   * Finds a trainer monthly summary by trainer username, year, and month.
   *
   * @param trainerUsername trainer username
   * @param trainingYear training year
   * @param trainingMonth training month
   * @return found monthly summary or empty result
   */
  Optional<TrainerMonthlySummary> findByTrainerUsernameAndTrainingYearAndTrainingMonth(
      String trainerUsername,
      int trainingYear,
      int trainingMonth
  );
}
