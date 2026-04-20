package com.epam.gymcrm.service;

import com.epam.gymcrm.model.Trainee;
import java.util.Optional;

public interface TraineeService {
    void create(Trainee trainee);
    Optional<Trainee> findById(Long id);
}