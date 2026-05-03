package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;

@Dao
public class TrainingDaoImpl implements TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Training training) {
        if (training.getTrainingId() != null) {
            entityManager.merge(training);
        } else {
            entityManager.persist(training);
        }
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Training.class, id));

    }

}
