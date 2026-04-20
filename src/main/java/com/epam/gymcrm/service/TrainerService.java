package com.epam.gymcrm.service;

import com.epam.gymcrm.model.Trainer;
import java.util.Optional;

public interface TrainerService {
    void create(Trainer trainer);
    Optional<Trainer> findById(Long id);
}