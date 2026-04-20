package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.storage.InMemoryStorage;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    void setStorage(InMemoryStorage storage);
    void save(Trainee trainee);
    Optional<Trainee> findById(Long id);
    void delete(Long id);
    List<Trainee> findAll();
}