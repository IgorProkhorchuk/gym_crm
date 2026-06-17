package com.epam.gymcrm.criteria;

import java.time.LocalDate;

/**
 * Optional filters for searching trainer trainings.
 *
 * @param fromDate lower inclusive training date boundary
 * @param toDate upper inclusive training date boundary
 * @param traineeName trainee first or last name fragment
 */
public record TrainerTrainingCriteria(LocalDate fromDate, LocalDate toDate, String traineeName) {

  public static TrainerTrainingCriteria empty() {
    return new TrainerTrainingCriteria(null, null, null);
  }
}
