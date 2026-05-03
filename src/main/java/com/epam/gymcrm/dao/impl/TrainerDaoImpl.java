package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@Dao
public class TrainerDaoImpl implements TrainerDao {

    @PersistenceContext
    private EntityManager entityManager;

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
    public void delete(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public List<Trainer> findAll() {
        return entityManager
                .createQuery("select t from Trainer t", Trainer.class)
                .getResultList();
    }

}
