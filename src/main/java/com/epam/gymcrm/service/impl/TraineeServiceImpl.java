package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ProfileStateException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.UsernameGenerator;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TraineeServiceImpl implements TraineeService {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final com.epam.gymcrm.dao.UserDao userDao;
    private final AuthenticationService authenticationService;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private static final String TRAINEE_ID_NULL_ERROR = "Trainee id must not be null";

    @Override
    public void create(Trainee trainee) {
        requireNonNull(trainee, "Trainee must not be null");
        validateUserForCreate(trainee.getUser(), "Trainee user must not be null");

        User user = trainee.getUser();

        log.info("Creating trainee profile");

        String baseUsername = user.getFirstName() + "." + user.getLastName();
        user.setUsername(usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName(),
                userDao.findUsernamesByPattern(baseUsername + "%")
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
    @Transactional
    public void activate(String username, String password) {
        log.info("Activating trainee profile");

        Trainee trainee = authenticationService.authenticateTrainee(username, password);
        changeActiveStatus(trainee, true, "Trainee profile is already active");

        log.info("Trainee profile activated, userId={}", trainee.getId());
    }

    @Override
    @Transactional
    public void deactivate(String username, String password) {
        log.info("Deactivating trainee profile");

        Trainee trainee = authenticationService.authenticateTrainee(username, password);
        changeActiveStatus(trainee, false, "Trainee profile is already inactive");

        log.info("Trainee profile deactivated, userId={}", trainee.getId());
    }

    @Override
    @Transactional
    public void deleteByUsername(String username, String password) {
        log.info("Deleting trainee profile");

        Trainee trainee = authenticationService.authenticateTrainee(username, password);
        traineeDao.delete(trainee.getId());

        log.info("Trainee profile deleted, username={}", username);
    }

    @Override
    @Transactional
    public List<Trainer> updateTrainers(String username, String password, List<String> trainerUsernames) {
        requireNonNull(trainerUsernames, "Trainer usernames must not be null");
        log.info("Updating trainee trainers list");

        Set<String> uniqueTrainerUsernames = normalizeTrainerUsernames(trainerUsernames);
        Trainee trainee = authenticationService.authenticateTrainee(username, password);
        List<Trainer> trainers = uniqueTrainerUsernames.stream()
                .map(trainerUsername -> trainerDao.findByUsername(trainerUsername)
                        .orElseThrow(() -> new EntityNotFoundException("Trainer profile not found")))
                .toList();

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);
        traineeDao.save(trainee);

        log.info("Trainee trainers list updated, userId={}", trainee.getId());
        return trainers;
    }

    @Override
    @Transactional
    public void update(String username, String password, Trainee trainee) {
        requireNonNull(trainee, "Trainee must not be null");
        requireNonNull(trainee.getId(), TRAINEE_ID_NULL_ERROR);
        validateUserForUpdate(trainee.getUser(), "Trainee user must not be null");
        log.info("Updating trainee profile, userId={}", trainee.getId());

        Trainee authenticatedTrainee = authenticationService.authenticateTrainee(username, password);
        assertAuthenticatedProfile(
                authenticatedTrainee.getId(),
                trainee.getId(),
                "Authenticated trainee does not match updated profile"
        );
        applyTraineeProfileChanges(authenticatedTrainee, trainee);
        traineeDao.save(authenticatedTrainee);

        log.info("Trainee profile updated, userId={}", authenticatedTrainee.getId());
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

    private static void validateUserForCreate(User user, String message) {
        requireNonNull(user, message);
        validateUserNameFields(user);
        requireNonNull(user.getActive(), "Active must not be null");
    }

    private static void validateUserForUpdate(User user, String message) {
        requireNonNull(user, message);
        validateUserNameFields(user);
    }

    private static void validateUserNameFields(User user) {
        requireNonBlank(user.getFirstName(), "First name must not be blank");
        requireNonBlank(user.getLastName(), "Last name must not be blank");
    }

    private static void assertAuthenticatedProfile(Long authenticatedId, Long updateId, String message) {
        if (!Objects.equals(authenticatedId, updateId)) {
            throw new AuthenticationException(message);
        }
    }

    private static void applyTraineeProfileChanges(Trainee target, Trainee source) {
        User targetUser = target.getUser();
        User sourceUser = source.getUser();

        targetUser.setFirstName(sourceUser.getFirstName());
        targetUser.setLastName(sourceUser.getLastName());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setAddress(source.getAddress());
    }

    private static Set<String> normalizeTrainerUsernames(List<String> trainerUsernames) {
        Set<String> uniqueTrainerUsernames = new LinkedHashSet<>();
        for (String trainerUsername : trainerUsernames) {
            requireNonBlank(trainerUsername, "Trainer username must not be blank");
            uniqueTrainerUsernames.add(trainerUsername);
        }
        return uniqueTrainerUsernames;
    }

    private void changeActiveStatus(Trainee trainee, boolean active, String errorMessage) {
        User user = trainee.getUser();
        if (Boolean.valueOf(active).equals(user.getActive())) {
            throw new ProfileStateException(errorMessage);
        }

        user.setActive(active);
        traineeDao.save(trainee);
    }
}
