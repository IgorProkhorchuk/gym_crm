package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrainingServiceImpl implements TrainingService {

    private TrainingDao trainingDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public void create(Training training) {
        requireNonNull(training, "Training must not be null");
        log.info("Creating training, trainingId={}", training.getTrainingId());
        try {
            trainingDao.save(training);
            log.info("Training created, trainingId={}", training.getTrainingId());
        } catch (RuntimeException e) {
            log.error("Failed to create training, trainingId={}", training.getTrainingId(), e);
            throw e;
        }
    }

    @Override
    public Training findById(Long id) {
        requireNonNull(id, "Training id must not be null");
        try {
            return trainingDao.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Training not found"));
        } catch (RuntimeException e) {
            log.error("Failed to find training, trainingId={}", id, e);
            throw e;
        }
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
