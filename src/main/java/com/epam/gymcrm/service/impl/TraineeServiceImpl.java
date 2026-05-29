package com.epam.gymcrm.service.impl;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireEachNonBlank;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonNull;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.logging.AuditContext;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.UsernameGenerator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TraineeServiceImpl implements TraineeService {

  private final TraineeDao traineeDao;
  private final TrainerDao trainerDao;
  private final UserDao userDao;
  private final AuthenticationService authenticationService;
  private final PasswordGenerator passwordGenerator;
  private final UsernameGenerator usernameGenerator;
  private final TraineeMapper traineeMapper;
  private final TrainerMapper trainerMapper;

  @Override
  public UsernamePasswordResponse create(CreateTraineeRequest request) {
    requireNonNull(request, "Trainee request must not be null");
    validateNameFields(request.firstName(), request.lastName());

    Trainee trainee = traineeMapper.toEntity(request);
    User user = trainee.getUser();

    log.info("Creating trainee profile");

    String baseUsername = user.getFirstName() + "." + user.getLastName();
    user.setUsername(
        usernameGenerator.generate(
            user.getFirstName(),
            user.getLastName(),
            userDao.findUsernamesByPattern(baseUsername + "%")));
    user.setPassword(passwordGenerator.generate());
    traineeDao.save(trainee);
    AuditContext.setAuthenticatedUser(ProfileType.TRAINEE, user.getUserId(), trainee.getId());
    log.info("Trainee profile created, userId={}, traineeId={}", user.getUserId(), trainee.getId());

    return new UsernamePasswordResponse(user.getUsername(), user.getPassword());
  }

  @Override
  @Transactional(readOnly = true)
  public TraineeProfileResponse getProfile(AuthRequest request) {
    log.info("Getting trainee profile");
    return traineeMapper.toProfileResponse(authenticateTrainee(request));
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordRequest request) {
    requireNonNull(request, "Change password request must not be null");
    requireNonBlank(request.newPassword(), "New password must not be blank");
    log.info("Changing trainee password");

    Trainee trainee =
        authenticationService.authenticateTrainee(request.username(), request.oldPassword());
    trainee.getUser().setPassword(request.newPassword());
    traineeDao.save(trainee);

    log.info(
        "Trainee password changed, userId={}, traineeId={}",
        trainee.getUser().getUserId(),
        trainee.getId());
  }

  @Override
  @Transactional
  public void switchActiveStatus(AuthRequest request) {
    log.info("Switching trainee active status");

    Trainee trainee = authenticateTrainee(request);
    trainee.getUser().switchActiveStatus();
    traineeDao.save(trainee);

    log.info(
        "Trainee active status switched, userId={}, traineeId={}, active={}",
        trainee.getUser().getUserId(),
        trainee.getId(),
        trainee.getUser().getActive());
  }

  @Override
  @Transactional
  public void deleteByUsername(AuthRequest request) {
    log.info("Deleting trainee profile");

    Trainee trainee = authenticateTrainee(request);
    traineeDao.delete(trainee.getId());

    log.info(
        "Trainee profile deleted, userId={}, traineeId={}",
        trainee.getUser().getUserId(),
        trainee.getId());
  }

  @Override
  @Transactional
  public List<TrainerSummaryResponse> updateTrainers(UpdateTraineeTrainersRequest request) {
    requireNonNull(request, "Update trainee trainers request must not be null");
    requireNonNull(request.trainerUsernames(), "Trainer usernames must not be null");
    log.info("Updating trainee trainers list");

    Set<String> uniqueTrainerUsernames = normalizeTrainerUsernames(request.trainerUsernames());
    Trainee trainee =
        authenticationService.authenticateTrainee(request.username(), request.password());
    List<Trainer> trainers =
        uniqueTrainerUsernames.stream()
            .map(
                trainerUsername ->
                    trainerDao
                        .findByUsername(trainerUsername)
                        .orElseThrow(
                            () -> new EntityNotFoundException("Trainer profile not found")))
            .toList();

    trainee.getTrainers().clear();
    trainee.getTrainers().addAll(trainers);
    traineeDao.save(trainee);

    log.info(
        "Trainee trainers list updated, userId={}, traineeId={}, trainersCount={}",
        trainee.getUser().getUserId(),
        trainee.getId(),
        trainers.size());
    return trainers.stream().map(trainerMapper::toSummaryResponse).toList();
  }

  @Override
  @Transactional
  public TraineeProfileResponse update(UpdateTraineeRequest request) {
    requireNonNull(request, "Update trainee request must not be null");
    validateNameFields(request.firstName(), request.lastName());
    requireNonNull(request.active(), "Active status must not be null");
    log.info("Updating trainee profile");

    Trainee authenticatedTrainee =
        authenticationService.authenticateTrainee(request.username(), request.password());
    traineeMapper.updateFromRequest(request, authenticatedTrainee);
    traineeDao.save(authenticatedTrainee);

    log.info(
        "Trainee profile updated, userId={}, traineeId={}",
        authenticatedTrainee.getUser().getUserId(),
        authenticatedTrainee.getId());
    return traineeMapper.toProfileResponse(authenticatedTrainee);
  }

  private static void validateNameFields(String firstName, String lastName) {
    requireNonBlank(firstName, "First name must not be blank");
    requireNonBlank(lastName, "Last name must not be blank");
  }

  private static Set<String> normalizeTrainerUsernames(List<String> trainerUsernames) {
    Set<String> uniqueTrainerUsernames = new LinkedHashSet<>();
    requireEachNonBlank(trainerUsernames, "Trainer username must not be blank");
    uniqueTrainerUsernames.addAll(trainerUsernames);
    return uniqueTrainerUsernames;
  }

  private Trainee authenticateTrainee(AuthRequest request) {
    requireNonNull(request, "Authentication request must not be null");
    return authenticationService.authenticateTrainee(request.username(), request.password());
  }
}
