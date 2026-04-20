package com.epam.gymcrm.service;

import com.epam.gymcrm.model.Training;
import java.util.Optional;

public interface TrainingService {
    void create(Training training);
    Optional<Training> findById(Long id);
}