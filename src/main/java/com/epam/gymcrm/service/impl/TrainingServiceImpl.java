package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingDao trainingDao;

    @Override
    public void create(Training training) {
        requireNonNull(training, "Training must not be null");
        log.info("Creating training, trainingId={}", training.getTrainingId());
        trainingDao.save(training);
        log.info("Training created, trainingId={}", training.getTrainingId());
    }

    @Override
    public Training findById(Long id) {
        requireNonNull(id, "Training id must not be null");
            return trainingDao.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Training not found"));
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
