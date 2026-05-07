package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ProfileStateException;
import com.epam.gymcrm.mapper.TrainerMapper;
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
    private final TrainerMapper trainerMapper;

    @Override
    public UsernamePasswordResponse create(CreateTrainerRequest request) {
        requireNonNull(request, "Trainer request must not be null");
        validateCreateRequest(request);

        Trainer trainer = trainerMapper.toEntity(request);
        trainer.setSpecialization(resolveSpecializationName(request.specialization()));
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

        return new UsernamePasswordResponse(user.getUsername(), user.getPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerProfileResponse getProfile(AuthRequest request) {
        log.info("Getting trainer profile");
        return trainerMapper.toProfileResponse(authenticateTrainer(request));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        requireNonNull(request, "Change password request must not be null");
        requireNonBlank(request.newPassword(), "New password must not be blank");
        log.info("Changing trainer password");

        Trainer trainer = authenticationService.authenticateTrainer(request.username(), request.oldPassword());
        trainer.getUser().setPassword(request.newPassword());
        trainerDao.save(trainer);

        log.info("Trainer password changed, userId={}", trainer.getId());
    }

    @Override
    @Transactional
    public void activate(AuthRequest request) {
        log.info("Activating trainer profile");

        Trainer trainer = authenticateTrainer(request);
        changeActiveStatus(trainer, true, "Trainer profile is already active");

        log.info("Trainer profile activated, userId={}", trainer.getId());
    }

    @Override
    @Transactional
    public void deactivate(AuthRequest request) {
        log.info("Deactivating trainer profile");

        Trainer trainer = authenticateTrainer(request);
        changeActiveStatus(trainer, false, "Trainer profile is already inactive");

        log.info("Trainer profile deactivated, userId={}", trainer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerSummaryResponse> getUnassignedTrainers(AuthRequest request) {
        requireNonNull(request, "Authentication request must not be null");
        log.info("Getting active trainers not assigned to trainee");

        authenticationService.authenticateTrainee(request.username(), request.password());
        return trainerDao.findNotAssignedToTrainee(request.username()).stream()
                .map(trainerMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional
    public TrainerProfileResponse update(UpdateTrainerRequest request) {
        requireNonNull(request, "Update trainer request must not be null");
        requireNonNull(request.id(), TRAINER_ID_NULL_ERROR);
        validateNameFields(request.firstName(), request.lastName());
        validateSpecializationName(request.specialization());
        log.info("Updating trainer profile, userId={}", request.id());

        Trainer authenticatedTrainer = authenticationService.authenticateTrainer(request.username(), request.password());
        assertAuthenticatedProfile(
                authenticatedTrainer.getId(),
                request.id(),
                "Authenticated trainer does not match updated profile"
        );
        trainerMapper.updateFromRequest(request, authenticatedTrainer);
        authenticatedTrainer.setSpecialization(resolveSpecializationName(request.specialization()));
        trainerDao.save(authenticatedTrainer);

        log.info("Trainer profile updated, userId={}", authenticatedTrainer.getId());
        return trainerMapper.toProfileResponse(authenticatedTrainer);
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

    private static void validateCreateRequest(CreateTrainerRequest request) {
        validateNameFields(request.firstName(), request.lastName());
        requireNonNull(request.active(), "Active must not be null");
        validateSpecializationName(request.specialization());
    }

    private static void validateNameFields(String firstName, String lastName) {
        requireNonBlank(firstName, "First name must not be blank");
        requireNonBlank(lastName, "Last name must not be blank");
    }

    private static void validateSpecializationName(String specialization) {
        requireNonNull(specialization, "Trainer specialization must not be null");
        requireNonBlank(specialization, "Trainer specialization must not be blank");
    }

    private static void assertAuthenticatedProfile(Long authenticatedId, Long updateId, String message) {
        if (!Objects.equals(authenticatedId, updateId)) {
            throw new AuthenticationException(message);
        }
    }

    private TrainingType resolveSpecializationName(String specialization) {
        validateSpecializationName(specialization);
        return trainingTypeDao.findByName(specialization)
                .orElseThrow(() -> new EntityNotFoundException("Training type not found"));
    }

    private Trainer authenticateTrainer(AuthRequest request) {
        requireNonNull(request, "Authentication request must not be null");
        return authenticationService.authenticateTrainer(request.username(), request.password());
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
