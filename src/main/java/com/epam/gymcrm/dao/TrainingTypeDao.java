package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.TrainingType;
import java.util.List;
import java.util.Optional;

/** Persistence contract for {@link TrainingType} records. */
public interface TrainingTypeDao {

  /**
   * Finds a training type by name.
   *
   * @param name training type name
   * @return training type with the given name, or {@link Optional#empty()} when absent
   */
  Optional<TrainingType> findByName(String name);

  /**
   * Returns all training types ordered by identifier.
   *
   * @return available training types
   */
  List<TrainingType> findAll();
}
