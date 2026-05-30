package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.model.Trainer;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Dao
public class TrainerDaoImpl implements TrainerDao {

  private final EntityManager entityManager;

  public TrainerDaoImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void save(Trainer trainer) {
    if (trainer.getId() == null) {
      entityManager.persist(trainer);
    } else {
      entityManager.merge(trainer);
    }
  }

  @Override
  public Optional<Trainer> findById(Long id) {
    return Optional.ofNullable(entityManager.find(Trainer.class, id));
  }

  @Override
  public Optional<Trainer> findByUsername(String username) {
    return entityManager
        .createQuery(
            """
                        select t
                        from Trainer t
                        join fetch t.user u
                        where u.username = :username
            """,
            Trainer.class)
        .setParameter("username", username)
        .getResultStream()
        .findFirst();
  }

  @Override
  public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
    return entityManager
        .createQuery(
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
            """,
            Trainer.class)
        .setParameter("traineeUsername", traineeUsername)
        .getResultList();
  }

  @Override
  public void delete(Long id) {
    findById(id).ifPresent(entityManager::remove);
  }

  @Override
  public List<Trainer> findAll(PageRequest pageRequest) {
    PageRequest page = Objects.requireNonNull(pageRequest, "Page request must not be null");
    return entityManager
        .createQuery("select t from Trainer t", Trainer.class)
        .setFirstResult(page.offset())
        .setMaxResults(page.limit())
        .getResultList();
  }
}
