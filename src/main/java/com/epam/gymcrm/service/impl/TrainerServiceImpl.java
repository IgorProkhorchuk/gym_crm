package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ProfileStateException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainerServiceImpl implements TrainerService {

    private static final String TRAINER_ID_NULL_ERROR = "Trainer id must not be null";

    private final TrainerDao trainerDao;
    private final UserDao userDao;
    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;

    @Override
    public void create(Trainer trainer) {
        requireNonNull(trainer, "Trainer must not be null");
        validateUserForCreate(trainer.getUser(), "Trainer user must not be null");
        trainer.setSpecialization(resolveSpecialization(trainer.getSpecialization()));

        User user = trainer.getUser();

        log.info("Creating trainer profile");
        String baseUsername = user.getFirstName() + "." + user.getLastName();
        user.setUsername(usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName(),
                userDao.findUsernamesByPattern(baseUsername + "%")
        ));

        user.setPassword(passwordGenerator.generate());
        trainerDao.save(trainer);
        log.info("Trainer profile created, Id={}, userId={}", trainer.getId(), user.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer getProfile(String username, String password) {
        log.info("Getting trainer profile");
        return authenticationService.authenticateTrainer(username, password);
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        requireNonBlank(newPassword, "New password must not be blank");
        log.info("Changing trainer password");

        Trainer trainer = authenticationService.authenticateTrainer(username, oldPassword);
        trainer.getUser().setPassword(newPassword);
        trainerDao.save(trainer);

        log.info("Trainer password changed, userId={}", trainer.getId());
    }

    @Override
    @Transactional
    public void activate(String username, String password) {
        log.info("Activating trainer profile");

        Trainer trainer = authenticationService.authenticateTrainer(username, password);
        changeActiveStatus(trainer, true, "Trainer profile is already active");

        log.info("Trainer profile activated, userId={}", trainer.getId());
    }

    @Override
    @Transactional
    public void deactivate(String username, String password) {
        log.info("Deactivating trainer profile");

        Trainer trainer = authenticationService.authenticateTrainer(username, password);
        changeActiveStatus(trainer, false, "Trainer profile is already inactive");

        log.info("Trainer profile deactivated, userId={}", trainer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername, String traineePassword) {
        log.info("Getting active trainers not assigned to trainee");

        authenticationService.authenticateTrainee(traineeUsername, traineePassword);
        return trainerDao.findNotAssignedToTrainee(traineeUsername);
    }

    @Override
    @Transactional
    public void update(String username, String password, Trainer trainer) {
        requireNonNull(trainer, "Trainer must not be null");
        requireNonNull(trainer.getId(), TRAINER_ID_NULL_ERROR);
        validateUserForUpdate(trainer.getUser(), "Trainer user must not be null");
        validateSpecializationForUpdate(trainer.getSpecialization());
        log.info("Updating trainer profile, userId={}", trainer.getId());

        Trainer authenticatedTrainer = authenticationService.authenticateTrainer(username, password);
        assertAuthenticatedProfile(
                authenticatedTrainer.getId(),
                trainer.getId(),
                "Authenticated trainer does not match updated profile"
        );
        applyTrainerProfileChanges(authenticatedTrainer, trainer, resolveSpecialization(trainer.getSpecialization()));
        trainerDao.save(authenticatedTrainer);

        log.info("Trainer profile updated, userId={}", authenticatedTrainer.getId());
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

    private static void validateSpecializationForUpdate(TrainingType specialization) {
        requireNonNull(specialization, "Trainer specialization must not be null");
        requireNonBlank(specialization.getTrainingTypeName(), "Trainer specialization must not be blank");
    }

    private static void assertAuthenticatedProfile(Long authenticatedId, Long updateId, String message) {
        if (!Objects.equals(authenticatedId, updateId)) {
            throw new AuthenticationException(message);
        }
    }

    private static void applyTrainerProfileChanges(Trainer target, Trainer source, TrainingType specialization) {
        User targetUser = target.getUser();
        User sourceUser = source.getUser();

        targetUser.setFirstName(sourceUser.getFirstName());
        targetUser.setLastName(sourceUser.getLastName());
        target.setSpecialization(specialization);
    }

    private TrainingType resolveSpecialization(TrainingType specialization) {
        validateSpecializationForUpdate(specialization);

        return trainingTypeDao.findByName(specialization.getTrainingTypeName())
                .orElseThrow(() -> new EntityNotFoundException("Training type not found"));
    }

    private void changeActiveStatus(Trainer trainer, boolean active, String errorMessage) {
        User user = trainer.getUser();
        if (Boolean.valueOf(active).equals(user.getActive())) {
            throw new ProfileStateException(errorMessage);
        }

        user.setActive(active);
        trainerDao.save(trainer);
    }
}
