package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.client.workload.TrainerWorkloadActionType;
import com.epam.gymcrm.client.workload.TrainerWorkloadClient;
import com.epam.gymcrm.client.workload.TrainerWorkloadRequest;
import com.epam.gymcrm.client.workload.TrainerWorkloadRequestFactory;
import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.PageRequest;
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
import com.epam.gymcrm.monitoring.metrics.GymMetrics;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
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

  private final TrainingRepository trainingRepository;
  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final TrainingTypeRepository trainingTypeRepository;
  private final TrainingMapper trainingMapper;
  private final GymMetrics gymMetrics;
  private final TrainerWorkloadClient trainerWorkloadClient;
  private final TrainerWorkloadRequestFactory trainerWorkloadRequestFactory;

  @Override
  public void addTraining(AddTrainingRequest request) {
    log.info("Adding training");

    Trainee trainee = findTrainingTrainee(request.traineeUsername());
    Trainer trainer =
        trainerRepository
            .findByUsername(request.trainerUsername())
            .orElseThrow(
                () -> {
                  gymMetrics.recordTrainingCreationTrainerNotFound();
                  return new EntityNotFoundException("Trainer profile not found");
                });
    TrainingType trainingType =
        trainingTypeRepository
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

    trainingRepository.save(training);
    trainerWorkloadClient.updateTrainerWorkload(
        trainerWorkloadRequestFactory.fromTraining(training, TrainerWorkloadActionType.ADD));
    gymMetrics.recordTrainingCreationSucceeded();

    log.info("Training added, trainingId={}", training.getTrainingId());
  }

  @Override
  public void deleteTraining(Long trainingId) {
    log.info("Deleting training, trainingId={}", trainingId);

    Training training = trainingRepository.findById(trainingId)
        .orElseThrow(() -> new EntityNotFoundException("Training not found"));
    TrainerWorkloadRequest workloadRequest =
        trainerWorkloadRequestFactory.fromTraining(training, TrainerWorkloadActionType.DELETE);

    trainingRepository.delete(training);
    trainerWorkloadClient.updateTrainerWorkload(workloadRequest);

    log.info("Training deleted, trainingId={}", trainingId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TraineeTrainingResponse> getTraineeTrainings(TraineeTrainingsRequest request) {
    log.info("Getting trainee trainings");

    TraineeTrainingCriteria criteria = trainingMapper.toCriteria(request);
    return trainingRepository
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

    TrainerTrainingCriteria criteria = trainingMapper.toCriteria(request);
    return trainingRepository
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

  private Trainee findTrainingTrainee(String username) {
    return traineeRepository
        .findByUsername(username)
        .orElseThrow(
            () -> {
              gymMetrics.recordTrainingCreationAuthFailed();
              return new EntityNotFoundException("Trainee profile not found");
            });
  }
}
