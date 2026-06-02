package com.epam.gymcrm.dao;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.model.Training;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Persistence contract for {@link Training} records keyed by {@link Training#getTrainingId()}. */
public interface TrainingDao extends JpaRepository<Training, Long> {

  /**
   * Finds trainee trainings by trainee username and optional criteria.
   *
   * @param traineeUsername trainee username to search trainings for
   * @param criteria optional filters
   * @param pageRequest pagination settings
   * @return trainings matching the given username, filters, and page
   */
  default List<Training> findByTraineeUsernameAndCriteria(
      String traineeUsername, TraineeTrainingCriteria criteria, PageRequest pageRequest) {
    TraineeTrainingCriteria effectiveCriteria =
        criteria == null ? TraineeTrainingCriteria.empty() : criteria;
    PageRequest page = Objects.requireNonNull(pageRequest, "Page request must not be null");
    return findByTraineeUsernameAndFilters(
        traineeUsername,
        fromDate(effectiveCriteria.fromDate()),
        toDate(effectiveCriteria.toDate()),
        toLikePattern(effectiveCriteria.trainerName()),
        toExactValue(effectiveCriteria.trainingType()),
        toSpringPageRequest(page));
  }

  /**
   * Finds trainer trainings by trainer username and optional criteria.
   *
   * @param trainerUsername trainer username to search trainings for
   * @param criteria optional filters
   * @param pageRequest pagination settings
   * @return trainings matching the given username, filters, and page
   */
  default List<Training> findByTrainerUsernameAndCriteria(
      String trainerUsername, TrainerTrainingCriteria criteria, PageRequest pageRequest) {
    TrainerTrainingCriteria effectiveCriteria =
        criteria == null ? TrainerTrainingCriteria.empty() : criteria;
    PageRequest page = Objects.requireNonNull(pageRequest, "Page request must not be null");
    return findByTrainerUsernameAndFilters(
        trainerUsername,
        fromDate(effectiveCriteria.fromDate()),
        toDate(effectiveCriteria.toDate()),
        toLikePattern(effectiveCriteria.traineeName()),
        toSpringPageRequest(page));
  }

  /**
   * Finds trainee trainings by normalized filter parameters.
   *
   * @param traineeUsername trainee username to search trainings for
   * @param fromDate lower inclusive training date boundary
   * @param toDate upper inclusive training date boundary
   * @param trainerName trainer first or last name like-pattern
   * @param trainingTypeName exact training type name
   * @param pageable pagination settings
   * @return trainings matching the given filters
   */
  @Query(
      """
          select tr
          from Training tr
          join fetch tr.trainee trainee
          join fetch trainee.user traineeUser
          join fetch tr.trainer trainer
          join fetch trainer.user trainerUser
          join fetch tr.trainingType trainingType
          where traineeUser.username = :traineeUsername
            and tr.trainingDate >= :fromDate
            and tr.trainingDate <= :toDate
            and (
                :trainerName = ''
                or lower(trainerUser.firstName) like :trainerName
                or lower(trainerUser.lastName) like :trainerName
            )
            and (:trainingTypeName = '' or trainingType.trainingTypeName = :trainingTypeName)
          order by tr.trainingDate
      """)
  List<Training> findByTraineeUsernameAndFilters(
      String traineeUsername,
      LocalDate fromDate,
      LocalDate toDate,
      String trainerName,
      String trainingTypeName,
      Pageable pageable);

  /**
   * Finds trainer trainings by normalized filter parameters.
   *
   * @param trainerUsername trainer username to search trainings for
   * @param fromDate lower inclusive training date boundary
   * @param toDate upper inclusive training date boundary
   * @param traineeName trainee first or last name like-pattern
   * @param pageable pagination settings
   * @return trainings matching the given filters
   */
  @Query(
      """
          select tr
          from Training tr
          join fetch tr.trainee trainee
          join fetch trainee.user traineeUser
          join fetch tr.trainer trainer
          join fetch trainer.user trainerUser
          join fetch tr.trainingType trainingType
          where trainerUser.username = :trainerUsername
            and tr.trainingDate >= :fromDate
            and tr.trainingDate <= :toDate
            and (
                :traineeName = ''
                or lower(traineeUser.firstName) like :traineeName
                or lower(traineeUser.lastName) like :traineeName
            )
          order by tr.trainingDate
      """)
  List<Training> findByTrainerUsernameAndFilters(
      String trainerUsername,
      LocalDate fromDate,
      LocalDate toDate,
      String traineeName,
      Pageable pageable);

  private static org.springframework.data.domain.PageRequest toSpringPageRequest(PageRequest page) {
    return org.springframework.data.domain.PageRequest.of(page.page(), page.size());
  }

  private static String toExactValue(String value) {
    return isBlank(value) ? "" : value;
  }

  private static String toLikePattern(String value) {
    return isBlank(value) ? "" : "%" + value.toLowerCase() + "%";
  }

  private static LocalDate fromDate(LocalDate fromDate) {
    return fromDate == null ? LocalDate.of(1, 1, 1) : fromDate;
  }

  private static LocalDate toDate(LocalDate toDate) {
    return toDate == null ? LocalDate.of(9999, 12, 31) : toDate;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
