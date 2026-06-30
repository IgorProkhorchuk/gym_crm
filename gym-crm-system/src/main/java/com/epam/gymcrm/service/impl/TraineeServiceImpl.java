package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.client.workload.TrainerWorkloadActionType;
import com.epam.gymcrm.client.workload.TrainerWorkloadOutboxService;
import com.epam.gymcrm.client.workload.TrainerWorkloadRequest;
import com.epam.gymcrm.client.workload.TrainerWorkloadRequestFactory;
import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.UserRepository;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.UsernameGenerator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TraineeServiceImpl implements TraineeService {

  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final TrainingRepository trainingRepository;
  private final UserRepository userRepository;
  private final AuthenticationService authenticationService;
  private final PasswordGenerator passwordGenerator;
  private final PasswordEncoder passwordEncoder;
  private final UsernameGenerator usernameGenerator;
  private final TraineeMapper traineeMapper;
  private final TrainerMapper trainerMapper;
  private final TrainerWorkloadOutboxService trainerWorkloadOutboxService;
  private final TrainerWorkloadRequestFactory trainerWorkloadRequestFactory;

  @Override
  public UsernamePasswordResponse create(CreateTraineeRequest request) {
    Trainee trainee = traineeMapper.toEntity(request);
    User user = trainee.getUser();

    log.info("Creating trainee profile");

    String baseUsername = user.getFirstName() + "." + user.getLastName();
    user.setUsername(
        usernameGenerator.generate(
            user.getFirstName(),
            user.getLastName(),
            userRepository.findUsernamesByPattern(baseUsername + "%")));
    String generatedPassword = passwordGenerator.generate();
    user.setPassword(passwordEncoder.encode(generatedPassword));
    traineeRepository.save(trainee);
    log.info("Trainee profile created, userId={}", trainee.getId());

    return new UsernamePasswordResponse(user.getUsername(), generatedPassword);
  }

  @Override
  @Transactional(readOnly = true)
  public TraineeProfileResponse getProfile(AuthRequest request) {
    log.info("Getting trainee profile");
    return traineeMapper.toProfileResponse(findTrainee(request.username()));
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordRequest request) {
    log.info("Changing trainee password");

    Trainee trainee =
        authenticationService.authenticateTrainee(request.username(), request.oldPassword());
    trainee.getUser().setPassword(passwordEncoder.encode(request.newPassword()));
    traineeRepository.save(trainee);

    log.info("Trainee password changed, userId={}", trainee.getId());
  }

  @Override
  @Transactional
  public void switchActiveStatus(AuthRequest request) {
    log.info("Switching trainee active status");

    Trainee trainee = findTrainee(request.username());
    trainee.getUser().switchActiveStatus();
    traineeRepository.save(trainee);

    log.info("Trainee active status switched, userId={}", trainee.getId());
  }

  @Override
  @Transactional
  public void deleteByUsername(AuthRequest request) {
    log.info("Deleting trainee profile");

    Trainee trainee = findTrainee(request.username());
    saveTrainerWorkloadDeleteEvents(trainee);
    traineeRepository.delete(trainee.getId());

    log.info("Trainee profile deleted, userId={}", trainee.getId());
  }

  @Override
  @Transactional
  public List<TrainerSummaryResponse> updateTrainers(UpdateTraineeTrainersRequest request) {
    log.info("Updating trainee trainers list");

    Set<String> uniqueTrainerUsernames = normalizeTrainerUsernames(request.trainerUsernames());
    Trainee trainee = findTrainee(request.username());
    List<Trainer> trainers =
        uniqueTrainerUsernames.stream()
            .map(
                trainerUsername ->
                    trainerRepository
                        .findByUsername(trainerUsername)
                        .orElseThrow(
                            () -> new EntityNotFoundException("Trainer profile not found")))
            .toList();

    trainee.getTrainers().clear();
    trainee.getTrainers().addAll(trainers);
    traineeRepository.save(trainee);

    log.info("Trainee trainers list updated, userId={}", trainee.getId());
    return trainers.stream().map(trainerMapper::toSummaryResponse).toList();
  }

  @Override
  @Transactional
  public TraineeProfileResponse update(UpdateTraineeRequest request) {
    log.info("Updating trainee profile");

    Trainee authenticatedTrainee = findTrainee(request.username());
    traineeMapper.updateFromRequest(request, authenticatedTrainee);
    traineeRepository.save(authenticatedTrainee);

    log.info("Trainee profile updated, userId={}", authenticatedTrainee.getId());
    return traineeMapper.toProfileResponse(authenticatedTrainee);
  }

  private static Set<String> normalizeTrainerUsernames(List<String> trainerUsernames) {
    Set<String> uniqueTrainerUsernames = new LinkedHashSet<>();
    uniqueTrainerUsernames.addAll(trainerUsernames);
    return uniqueTrainerUsernames;
  }

  private Trainee findTrainee(String username) {
    return traineeRepository
        .findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("Trainee profile not found"));
  }

  private void saveTrainerWorkloadDeleteEvents(Trainee trainee) {
    List<Training> trainings = trainingRepository.findByTraineeIdWithTrainer(trainee.getId());
    trainings.forEach(
        training -> {
          TrainerWorkloadRequest workloadRequest =
              trainerWorkloadRequestFactory.fromTraining(training, TrainerWorkloadActionType.DELETE);
          trainerWorkloadOutboxService.savePendingEvent(training.getTrainingId(), workloadRequest);
        });
    log.info(
        "Trainer workload delete events saved for trainee deletion, userId={}, eventCount={}",
        trainee.getId(),
        trainings.size());
  }
}
