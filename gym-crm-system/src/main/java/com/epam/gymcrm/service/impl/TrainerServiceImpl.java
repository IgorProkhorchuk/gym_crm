package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.repository.UserRepository;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.UsernameGenerator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainerServiceImpl implements TrainerService {

  private final TrainerRepository trainerRepository;
  private final TraineeRepository traineeRepository;
  private final UserRepository userRepository;
  private final TrainingTypeRepository trainingTypeRepository;
  private final AuthenticationService authenticationService;
  private final PasswordGenerator passwordGenerator;
  private final PasswordEncoder passwordEncoder;
  private final UsernameGenerator usernameGenerator;
  private final TrainerMapper trainerMapper;

  @Override
  public UsernamePasswordResponse create(CreateTrainerRequest request) {
    Trainer trainer = trainerMapper.toEntity(request);
    trainer.setSpecialization(resolveSpecializationName(request.specialization()));
    User user = trainer.getUser();

    log.info("Creating trainer profile");
    String baseUsername = user.getFirstName() + "." + user.getLastName();
    user.setUsername(
        usernameGenerator.generate(
            user.getFirstName(),
            user.getLastName(),
            userRepository.findUsernamesByPattern(baseUsername + "%")));

    String generatedPassword = passwordGenerator.generate();
    user.setPassword(passwordEncoder.encode(generatedPassword));
    trainerRepository.save(trainer);
    log.info("Trainer profile created, Id={}, userId={}", trainer.getId(), user.getUserId());

    return new UsernamePasswordResponse(user.getUsername(), generatedPassword);
  }

  @Override
  @Transactional(readOnly = true)
  public TrainerProfileResponse getProfile(AuthRequest request) {
    log.info("Getting trainer profile");
    return trainerMapper.toProfileResponse(findTrainer(request.username()));
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordRequest request) {
    log.info("Changing trainer password");

    Trainer trainer =
        authenticationService.authenticateTrainer(request.username(), request.oldPassword());
    trainer.getUser().setPassword(passwordEncoder.encode(request.newPassword()));
    trainerRepository.save(trainer);

    log.info("Trainer password changed, userId={}", trainer.getId());
  }

  @Override
  @Transactional
  public void switchActiveStatus(AuthRequest request) {
    log.info("Switching trainer active status");

    Trainer trainer = findTrainer(request.username());
    trainer.getUser().switchActiveStatus();
    trainerRepository.save(trainer);

    log.info("Trainer active status switched, userId={}", trainer.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrainerSummaryResponse> getUnassignedTrainers(AuthRequest request) {
    log.info("Getting active trainers not assigned to trainee");

    traineeRepository
        .findByUsername(request.username())
        .orElseThrow(() -> new EntityNotFoundException("Trainee profile not found"));
    return trainerRepository.findNotAssignedToTrainee(request.username()).stream()
        .map(trainerMapper::toSummaryResponse)
        .toList();
  }

  @Override
  @Transactional
  public TrainerProfileResponse update(UpdateTrainerRequest request) {
    log.info("Updating trainer profile");

    Trainer authenticatedTrainer = findTrainer(request.username());
    trainerMapper.updateFromRequest(request, authenticatedTrainer);
    authenticatedTrainer.setSpecialization(resolveSpecializationName(request.specialization()));
    trainerRepository.save(authenticatedTrainer);

    log.info("Trainer profile updated, userId={}", authenticatedTrainer.getId());
    return trainerMapper.toProfileResponse(authenticatedTrainer);
  }

  private TrainingType resolveSpecializationName(String specialization) {
    return trainingTypeRepository
        .findByName(specialization)
        .orElseThrow(() -> new EntityNotFoundException("Training type not found"));
  }

  private Trainer findTrainer(String username) {
    return trainerRepository
        .findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("Trainer profile not found"));
  }
}
