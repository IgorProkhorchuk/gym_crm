package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.monitoring.metrics.GymMetrics;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.TrainingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class TrainingServiceImpl implements TrainingService {

  private final TrainingDao trainingDao;
  private final TrainerDao trainerDao;
  private final TrainingTypeDao trainingTypeDao;
  private final AuthenticationService authenticationService;
  private final TrainingMapper trainingMapper;
  private final GymMetrics gymMetrics;

  @Override
  public void addTraining(AddTrainingRequest request) {
    log.info("Adding training");

    Trainee trainee = authenticateTrainingTrainee(request);
    Trainer trainer =
        trainerDao
            .findByUsername(request.trainerUsername())
            .orElseThrow(
                () -> {
                  gymMetrics.recordTrainingCreationTrainerNotFound();
                  return new EntityNotFoundException("Trainer profile not found");
                });
    TrainingType trainingType =
        trainingTypeDao
            .findByName(request.trainingTypeName())
            .orElseThrow(
                () -> {
                  gymMetrics.recordTrainingCreationTrainingTypeNotFound();
                  return new EntityNotFoundException("Training type not found");
                });

    Training training = trainingMapper.toEntity(request);
    training.setTrainee(trainee);
    training.setTrainer(trainer);
    training.setTrainingType(trainingType);

    trainingDao.save(training);
    gymMetrics.recordTrainingCreationSucceeded();

    log.info("Training added, trainingId={}", training.getTrainingId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<TraineeTrainingResponse> getTraineeTrainings(TraineeTrainingsRequest request) {
    log.info("Getting trainee trainings");

    authenticationService.authenticateTrainee(request.username(), request.password());
    TraineeTrainingCriteria criteria = trainingMapper.toCriteria(request);
    return trainingDao
        .findByTraineeUsernameAndCriteria(
            request.username(),
            criteria == null ? TraineeTrainingCriteria.empty() : criteria,
            page(request.pageRequest()))
        .stream()
        .map(trainingMapper::toTraineeTrainingResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrainerTrainingResponse> getTrainerTrainings(TrainerTrainingsRequest request) {
    log.info("Getting trainer trainings");

    authenticationService.authenticateTrainer(request.username(), request.password());
    TrainerTrainingCriteria criteria = trainingMapper.toCriteria(request);
    return trainingDao
        .findByTrainerUsernameAndCriteria(
            request.username(),
            criteria == null ? TrainerTrainingCriteria.empty() : criteria,
            page(request.pageRequest()))
        .stream()
        .map(trainingMapper::toTrainerTrainingResponse)
        .toList();
  }

  private static PageRequest page(PageRequest pageRequest) {
    return pageRequest == null ? PageRequest.firstPage() : pageRequest;
  }

  private Trainee authenticateTrainingTrainee(AddTrainingRequest request) {
    try {
      return authenticationService.authenticateTrainee(
          request.traineeUsername(), request.traineePassword());
    } catch (AuthenticationException exception) {
      gymMetrics.recordTrainingCreationAuthFailed();
      throw exception;
    }
  }
}
