package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Dao
public class TraineeDaoImpl implements TraineeDao {

    @PersistenceContext
    private EntityManager entityManager;

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
    public void delete(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public List<Trainee> findAll() {
        return entityManager.createQuery("SELECT t FROM Trainee t", Trainee.class).getResultList();
    }

}
