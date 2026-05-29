package com.epam.gymcrm.service.impl;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.logging.AuditContext;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

  private static final String INVALID_CREDENTIALS_ERROR = "Invalid username or password";

  private final TraineeDao traineeDao;
  private final TrainerDao trainerDao;

  @Override
  @Transactional(readOnly = true)
  public Trainee authenticateTrainee(String username, String password) {
    requireNonBlank(username, "Username must not be blank");
    requireNonBlank(password, "Password must not be blank");

    log.info("Authenticating trainee profile");

    Trainee trainee =
        traineeDao
        .findByUsername(username)
        .filter(foundTrainee -> password.equals(foundTrainee.getUser().getPassword()))
        .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS_ERROR));
    AuditContext.setAuthenticatedUser(
        ProfileType.TRAINEE, trainee.getUser().getUserId(), trainee.getId());
    return trainee;
  }

  @Override
  @Transactional(readOnly = true)
  public Trainer authenticateTrainer(String username, String password) {
    requireNonBlank(username, "Username must not be blank");
    requireNonBlank(password, "Password must not be blank");

    log.info("Authenticating trainer profile");

    Trainer trainer =
        trainerDao
        .findByUsername(username)
        .filter(foundTrainer -> password.equals(foundTrainer.getUser().getPassword()))
        .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS_ERROR));
    AuditContext.setAuthenticatedUser(
        ProfileType.TRAINER, trainer.getUser().getUserId(), trainer.getId());
    return trainer;
  }
}
