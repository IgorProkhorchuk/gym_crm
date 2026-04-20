package com.epam.gymcrm.service;

import com.epam.gymcrm.model.Trainee;
import java.util.Optional;

public interface TraineeService {
    void create(Trainee trainee);
    void update(Trainee trainee);
    void delete(Long id);
    Optional<Trainee> findById(Long id);
}
