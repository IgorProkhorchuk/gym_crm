package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TraineeDao {

    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    public void save(Trainee trainee) {
        storage.getStorage(Trainee.class).put(trainee.getUserId(), trainee);
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.getStorage(Trainee.class).get(id));
    }

    public void delete(Long id) {
        storage.getStorage(Trainee.class).remove(id);
    }


}
