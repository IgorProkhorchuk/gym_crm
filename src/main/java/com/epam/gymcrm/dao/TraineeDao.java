package com.epam.gymcrm.dao;

import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.model.Trainee;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Persistence contract for {@link Trainee} records keyed by {@link Trainee#getId()}. */
public interface TraineeDao extends JpaRepository<Trainee, Long> {

  /**
   * Finds a trainee by profile id.
   *
   * @param id trainee profile id to look up
   * @return trainee with the given id, or {@link Optional#empty()} when absent
   */
  Optional<Trainee> findById(Long id);

  /**
   * Finds a trainee by the username stored in the associated user record.
   *
   * @param username trainee username to look up
   * @return trainee with the given username, or {@link Optional#empty()} when absent
   */
  @Query(
      """
          select t
          from Trainee t
          join fetch t.user u
          where u.username = :username
      """)
  Optional<Trainee> findByUsername(String username);

  /**
   * Removes a trainee by profile id.
   *
   * @param id trainee profile id to remove
   */
  default void delete(Long id) {
    findById(id).ifPresent(this::delete);
  }

  /**
   * Returns a page of stored trainees.
   *
   * @param pageRequest pagination settings
   * @return trainees present on the requested page
   */
  default List<Trainee> findAll(PageRequest pageRequest) {
    PageRequest page = Objects.requireNonNull(pageRequest, "Page request must not be null");
    return findAll(org.springframework.data.domain.PageRequest.of(page.page(), page.size()))
        .getContent();
  }
}
