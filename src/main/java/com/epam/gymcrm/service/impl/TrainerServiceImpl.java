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
import com.epam.gymcrm.exception.EntityNotFoundException;
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

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainerServiceImpl implements TrainerService {

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
        validateNameFields(request.firstName(), request.lastName());
        validateSpecializationName(request.specialization());

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
    public void switchActiveStatus(AuthRequest request) {
        log.info("Switching trainer active status");

        Trainer trainer = authenticateTrainer(request);
        trainer.getUser().switchActiveStatus();
        trainerDao.save(trainer);

        log.info("Trainer active status switched, userId={}", trainer.getId());
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
        validateNameFields(request.firstName(), request.lastName());
        validateSpecializationName(request.specialization());
        log.info("Updating trainer profile");

        Trainer authenticatedTrainer = authenticationService.authenticateTrainer(request.username(), request.password());
        trainerMapper.updateFromRequest(request, authenticatedTrainer);
        authenticatedTrainer.setSpecialization(resolveSpecializationName(request.specialization()));
        trainerDao.save(authenticatedTrainer);

        log.info("Trainer profile updated, userId={}", authenticatedTrainer.getId());
        return trainerMapper.toProfileResponse(authenticatedTrainer);
    }

    private static void validateNameFields(String firstName, String lastName) {
        requireNonBlank(firstName, "First name must not be blank");
        requireNonBlank(lastName, "Last name must not be blank");
    }

    private static void validateSpecializationName(String specialization) {
        requireNonNull(specialization, "Trainer specialization must not be null");
        requireNonBlank(specialization, "Trainer specialization must not be blank");
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

}
