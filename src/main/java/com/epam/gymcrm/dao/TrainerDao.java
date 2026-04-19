package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainerDao {
    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public void save(Trainer trainer) {
        storage.getStorage(Trainer.class).put(trainer.getUserId(), trainer);
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.getStorage(Trainer.class).get(id));
    }

    public void delete(Long id) {
        storage.getStorage(Trainer.class).remove(id);
    }
}
