package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class TrainingDao {
    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public void save(Training training) {
        storage.getStorage(Training.class).put(training.getTrainingId(), training);
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.getStorage(Training.class).get(id));
    }

    public Collection<Training> findAll() {
        return storage.getStorage(Training.class).values();
    }
}
