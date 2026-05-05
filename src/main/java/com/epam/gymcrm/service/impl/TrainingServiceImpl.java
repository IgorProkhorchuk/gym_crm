package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainingServiceImpl implements TrainingService {

    private static final String TRAINING_ID_NULL_ERROR = "Training id must not be null";


    private final TrainingDao trainingDao;
    private final AuthenticationService authenticationService;

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

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username, String password, TraineeTrainingCriteria criteria) {
        log.info("Getting trainee trainings");

        authenticationService.authenticateTrainee(username, password);
        return trainingDao.findByTraineeUsernameAndCriteria(
                username,
                criteria == null ? TraineeTrainingCriteria.empty() : criteria
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username, String password, TrainerTrainingCriteria criteria) {
        log.info("Getting trainer trainings");

        authenticationService.authenticateTrainer(username, password);
        return trainingDao.findByTrainerUsernameAndCriteria(
                username,
                criteria == null ? TrainerTrainingCriteria.empty() : criteria
        );
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
