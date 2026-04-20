package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.storage.InMemoryStorage;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    void setStorage(InMemoryStorage storage);
    void save(Trainer trainer);
    Optional<Trainer> findById(Long id);
    void delete(Long id);
    List<Trainer> findAll();
}