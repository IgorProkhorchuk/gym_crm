package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainingServiceImpl implements TrainingService {

    private static final String TRAINING_ID_NULL_ERROR = "Training id must not be null";


    private final TrainingDao trainingDao;

    @Override
    public void create(Training training) {
        requireNonNull(training, "Training must not be null");
        requireNonNull(training.getTrainee(), "Training trainee must not be null");
        requireNonNull(training.getTrainer(), "Training trainer must not be null");
        requireNonNull(training.getTrainingType(), "Training type must not be null");

        log.info("Creating training, trainingId={}", training.getTrainingId());

        trainingDao.save(training);

        log.info("Training created, trainingId={}", training.getTrainingId());
    }

    @Override
    @Transactional(readOnly = true)
    public Training findById(Long id) {
        requireNonNull(id, TRAINING_ID_NULL_ERROR);
            return trainingDao.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Training not found"));
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
