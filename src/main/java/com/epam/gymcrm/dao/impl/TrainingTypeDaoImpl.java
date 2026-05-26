package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.model.TrainingType;
import jakarta.persistence.EntityManager;
import java.util.Optional;

@Dao
public class TrainingTypeDaoImpl implements TrainingTypeDao {

  private final EntityManager entityManager;

  public TrainingTypeDaoImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public Optional<TrainingType> findByName(String name) {
    return entityManager
        .createQuery(
            """
                        select tt
                        from TrainingType tt
                        where tt.trainingTypeName = :name
            """,
            TrainingType.class)
        .setParameter("name", name)
        .getResultStream()
        .findFirst();
  }
}
