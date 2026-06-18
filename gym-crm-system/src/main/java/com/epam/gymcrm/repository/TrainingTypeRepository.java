package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.TrainingType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repository contract for {@link TrainingType} records. */
public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {

  /**
   * Finds a training type by name.
   *
   * @param name training type name
   * @return training type with the given name, or {@link Optional#empty()} when absent
   */
  @Query("select tt from TrainingType tt where tt.trainingTypeName = :name")
  Optional<TrainingType> findByName(String name);

  /**
   * Returns all training types ordered by identifier.
   *
   * @return available training types
   */
  List<TrainingType> findAllByOrderByTrainingTypeIdAsc();
}
