package com.epam.gymcrm.criteria;

import java.time.LocalDate;

/**
 * Optional filters for searching trainee trainings.
 *
 * @param fromDate lower inclusive training date boundary
 * @param toDate upper inclusive training date boundary
 * @param trainerName trainer first or last name fragment
 * @param trainingType training type name
 */
public record TraineeTrainingCriteria(
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        String trainingType
) {

    public static TraineeTrainingCriteria empty() {
        return new TraineeTrainingCriteria(null, null, null, null);
    }
}
