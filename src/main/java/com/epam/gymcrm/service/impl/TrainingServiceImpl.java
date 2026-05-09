package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainingMapper;
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

    private final TrainingDao trainingDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;
    private final TrainingMapper trainingMapper;

    @Override
    public void addTraining(AddTrainingRequest request) {
        requireNonNull(request, "Training request must not be null");
        requireNonBlank(request.traineeUsername(), "Trainee username must not be blank");
        requireNonBlank(request.traineePassword(), "Trainee password must not be blank");
        requireNonBlank(request.trainerUsername(), "Trainer username must not be blank");
        requireNonBlank(request.trainingName(), "Training name must not be blank");
        requireNonBlank(request.trainingTypeName(), "Training type must not be blank");
        requireNonNull(request.trainingDate(), "Training date must not be null");
        requirePositive(request.trainingDuration(), "Training duration must be positive");

        log.info("Adding training");

        Trainee trainee = authenticationService.authenticateTrainee(request.traineeUsername(), request.traineePassword());
        Trainer trainer = trainerDao.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainer profile not found"));
        TrainingType trainingType = trainingTypeDao.findByName(request.trainingTypeName())
                .orElseThrow(() -> new EntityNotFoundException("Training type not found"));

        Training training = trainingMapper.toEntity(request);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);

        trainingDao.save(training);

        log.info("Training added, trainingId={}", training.getTrainingId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TraineeTrainingResponse> getTraineeTrainings(TraineeTrainingsRequest request) {
        requireNonNull(request, "Trainee trainings request must not be null");
        log.info("Getting trainee trainings");

        authenticationService.authenticateTrainee(request.username(), request.password());
        TraineeTrainingCriteria criteria = trainingMapper.toCriteria(request);
        return trainingDao.findByTraineeUsernameAndCriteria(
                request.username(),
                criteria == null ? TraineeTrainingCriteria.empty() : criteria
        ).stream()
                .map(trainingMapper::toTraineeTrainingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerTrainingResponse> getTrainerTrainings(TrainerTrainingsRequest request) {
        requireNonNull(request, "Trainer trainings request must not be null");
        log.info("Getting trainer trainings");

        authenticationService.authenticateTrainer(request.username(), request.password());
        TrainerTrainingCriteria criteria = trainingMapper.toCriteria(request);
        return trainingDao.findByTrainerUsernameAndCriteria(
                request.username(),
                criteria == null ? TrainerTrainingCriteria.empty() : criteria
        ).stream()
                .map(trainingMapper::toTrainerTrainingResponse)
                .toList();
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
