package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    private InMemoryStorage storage;

    @Autowired
    @Override
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public void save(Training training) {
        storage.getStorage(Training.class).put(training.getTrainingId(), training);
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.getStorage(Training.class).get(id));
    }
}
