package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.storage.InMemoryStorage;

import java.util.Optional;

public interface TrainingDao {
    void setStorage(InMemoryStorage storage);
    void save(Training training);
    Optional<Training> findById(Long id);
}