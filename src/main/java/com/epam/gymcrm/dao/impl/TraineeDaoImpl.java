package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Dao
public class TraineeDaoImpl implements TraineeDao {

    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public void save(Trainee trainee) {
        storage.getStorage(Trainee.class).put(trainee.getUserId(), trainee);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.getStorage(Trainee.class).get(id));
    }

    @Override
    public void delete(Long id) {
        storage.getStorage(Trainee.class).remove(id);
    }

    @Override
    public List<Trainee> findAll() {
        return List.copyOf(storage.getStorage(Trainee.class).values());
    }
}
