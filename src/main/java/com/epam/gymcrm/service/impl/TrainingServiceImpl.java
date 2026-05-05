package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.AddTrainingRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
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
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
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
    public void addTraining(String traineeUsername, String traineePassword, AddTrainingRequest request) {
        requireNonNull(request, "Training request must not be null");
        requireNonBlank(request.trainerUsername(), "Trainer username must not be blank");
        requireNonBlank(request.trainingName(), "Training name must not be blank");
        requireNonBlank(request.trainingTypeName(), "Training type must not be blank");
        requireNonNull(request.trainingDate(), "Training date must not be null");
        requirePositive(request.trainingDuration(), "Training duration must be positive");

        log.info("Adding training");

        Trainee trainee = authenticationService.authenticateTrainee(traineeUsername, traineePassword);
        Trainer trainer = trainerDao.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainer profile not found"));
        TrainingType trainingType = trainingTypeDao.findByName(request.trainingTypeName())
                .orElseThrow(() -> new EntityNotFoundException("Training type not found"));

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.trainingName())
                .trainingType(trainingType)
                .trainingDate(request.trainingDate())
                .trainingDuration(request.trainingDuration())
                .build();

        trainingDao.save(training);

        log.info("Training added, trainingId={}", training.getTrainingId());
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

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
