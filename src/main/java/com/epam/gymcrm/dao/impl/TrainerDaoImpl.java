package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Dao
public class TrainerDaoImpl implements TrainerDao {

    private InMemoryStorage storage;

    @Autowired
    @Override
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public void save(Trainer trainer) {
        storage.getStorage(Trainer.class).put(trainer.getUserId(), trainer);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.getStorage(Trainer.class).get(id));
    }

    @Override
    public void delete(Long id) {
        storage.getStorage(Trainer.class).remove(id);
    }

    @Override
    public List<Trainer> findAll() {
        return List.copyOf(storage.getStorage(Trainer.class).values());
    }
}
