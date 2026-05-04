package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.UsernameGenerator;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeDao traineeDao;
    private final AuthenticationService authenticationService;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private static final String TRAINEE_ID_NULL_ERROR = "Trainee id must not be null";

    @Override
    public void create(Trainee trainee) {
        requireNonNull(trainee, "Trainee must not be null");
        requireNonNull(trainee.getUser(), "Trainee user must not be null");

        User user = trainee.getUser();

        log.info("Creating trainee profile");

        user.setUsername(usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName(),
                traineeDao.findAll().stream()
                        .map(Trainee::getUser)
                        .filter(Objects::nonNull)
                        .map(User::getUsername)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        ));
        user.setPassword(passwordGenerator.generate());
        traineeDao.save(trainee);
        log.info("Trainee profile created, userId={}", trainee.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Trainee getProfile(String username, String password) {
        log.info("Getting trainee profile");
        return authenticationService.authenticateTrainee(username, password);
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        requireNonBlank(newPassword, "New password must not be blank");
        log.info("Changing trainee password");

        Trainee trainee = authenticationService.authenticateTrainee(username, oldPassword);
        trainee.getUser().setPassword(newPassword);
        traineeDao.save(trainee);

        log.info("Trainee password changed, userId={}", trainee.getId());
    }

    @Override
    public void update(Trainee trainee) {
        requireNonNull(trainee, "Trainee must not be null");
        requireNonNull(trainee.getId(), TRAINEE_ID_NULL_ERROR);
        log.info("Updating trainee profile, userId={}", trainee.getId());
        traineeDao.findById(trainee.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee profile not found"));
        traineeDao.save(trainee);
        log.info("Trainee profile updated, userId={}", trainee.getId());
    }

    @Override
    public void delete(Long id) {
        requireNonNull(id, TRAINEE_ID_NULL_ERROR);
        log.info("Deleting trainee profile, userId={}", id);

        traineeDao.delete(id);

        log.info("Trainee profile deleted, userId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Trainee findById(Long id) {
        requireNonNull(id, TRAINEE_ID_NULL_ERROR);

        return traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee profile not found"));
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
}
