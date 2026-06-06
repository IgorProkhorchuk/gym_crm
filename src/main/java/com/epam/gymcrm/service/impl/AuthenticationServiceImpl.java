package com.epam.gymcrm.service.impl;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
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

  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;

  @Override
  @Transactional(readOnly = true)
  public Trainee authenticateTrainee(String username, String password) {
    requireNonBlank(username, "Username must not be blank");
    requireNonBlank(password, "Password must not be blank");

    log.info("Authenticating trainee profile");

    return traineeRepository
        .findByUsername(username)
        .filter(trainee -> password.equals(trainee.getUser().getPassword()))
        .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS_ERROR));
  }

  @Override
  @Transactional(readOnly = true)
  public Trainer authenticateTrainer(String username, String password) {
    requireNonBlank(username, "Username must not be blank");
    requireNonBlank(password, "Password must not be blank");

    log.info("Authenticating trainer profile");

    return trainerRepository
        .findByUsername(username)
        .filter(trainer -> password.equals(trainer.getUser().getPassword()))
        .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS_ERROR));
  }
}
