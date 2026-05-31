package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.model.Trainee;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Dao
public class TraineeDaoImpl implements TraineeDao {

  private final EntityManager entityManager;

  public TraineeDaoImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void save(Trainee trainee) {
    if (trainee.getId() != null) {
      entityManager.merge(trainee);
    } else {
      entityManager.persist(trainee);
    }
  }

  @Override
  public Optional<Trainee> findById(Long id) {
    return Optional.ofNullable(entityManager.find(Trainee.class, id));
  }

  @Override
  public Optional<Trainee> findByUsername(String username) {
    return entityManager
        .createQuery(
            """
                        select t
                        from Trainee t
                        join fetch t.user u
                        where u.username = :username
            """,
            Trainee.class)
        .setParameter("username", username)
        .getResultStream()
        .findFirst();
  }

  @Override
  public void delete(Long id) {
    findById(id).ifPresent(entityManager::remove);
  }

  @Override
  public List<Trainee> findAll(PageRequest pageRequest) {
    PageRequest page = Objects.requireNonNull(pageRequest, "Page request must not be null");
    return entityManager
        .createQuery("SELECT t FROM Trainee t", Trainee.class)
        .setFirstResult(page.offset())
        .setMaxResults(page.limit())
        .getResultList();
  }
}
