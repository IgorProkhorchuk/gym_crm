package com.epam.gymcrm.dao;

import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.model.Trainer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Persistence contract for {@link Trainer} records keyed by {@link Trainer#getId()}. */
public interface TrainerDao extends JpaRepository<Trainer, Long> {

  /**
   * Finds a trainer by profile id.
   *
   * @param id trainer profile id to look up
   * @return trainer with the given id, or {@link Optional#empty()} when absent
   */
  Optional<Trainer> findById(Long id);

  /**
   * Finds a trainer by the username stored in the associated user record.
   *
   * @param username trainer username to look up
   * @return trainer with the given username, or {@link Optional#empty()} when absent
   */
  @Query(
      """
          select t
          from Trainer t
          join fetch t.user u
          where u.username = :username
      """)
  Optional<Trainer> findByUsername(String username);

  /**
   * Finds active trainers that are not assigned to the trainee with the given username.
   *
   * @param traineeUsername trainee username to check assignments for
   * @return active trainers not assigned to the trainee, or an empty list when the trainee does not
   *     exist
   */
  @Query(
      """
          select distinct t
          from Trainer t
          join fetch t.user u
          where u.active = true
            and exists (
                select trainee.id
                from Trainee trainee
                join trainee.user traineeUser
                where traineeUser.username = :traineeUsername
            )
            and not exists (
                select assignedTrainer.id
                from Trainee trainee
                join trainee.user traineeUser
                join trainee.trainers assignedTrainer
                where traineeUser.username = :traineeUsername
                  and assignedTrainer = t
            )
          order by u.firstName, u.lastName, u.username
      """)
  List<Trainer> findNotAssignedToTrainee(String traineeUsername);

  /**
   * Removes a trainer by profile id.
   *
   * @param id trainer profile id to remove
   */
  default void delete(Long id) {
    findById(id).ifPresent(this::delete);
  }

  /**
   * Returns a page of stored trainers.
   *
   * @param pageRequest pagination settings
   * @return trainers present on the requested page
   */
  default List<Trainer> findAll(PageRequest pageRequest) {
    PageRequest page = Objects.requireNonNull(pageRequest, "Page request must not be null");
    return findAll(org.springframework.data.domain.PageRequest.of(page.page(), page.size()))
        .getContent();
  }
}
