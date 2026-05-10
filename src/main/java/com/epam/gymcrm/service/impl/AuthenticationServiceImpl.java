package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;

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

        return traineeDao.findByUsername(username)
                .filter(trainee -> password.equals(trainee.getUser().getPassword()))
                .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS_ERROR));
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer authenticateTrainer(String username, String password) {
        requireNonBlank(username, "Username must not be blank");
        requireNonBlank(password, "Password must not be blank");

        log.info("Authenticating trainer profile");

        return trainerDao.findByUsername(username)
                .filter(trainer -> password.equals(trainer.getUser().getPassword()))
                .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS_ERROR));
    }
}
