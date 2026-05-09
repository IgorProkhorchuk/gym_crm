package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ProfileStateException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        validateCreateRequest(request);

        Trainee trainee = traineeMapper.toEntity(request);
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

        Trainee trainee = authenticationService.authenticateTrainee(request.username(), request.oldPassword());
        trainee.getUser().setPassword(request.newPassword());
        traineeDao.save(trainee);

        log.info("Trainee password changed, userId={}", trainee.getId());
    }

    @Override
    @Transactional
    public void activate(AuthRequest request) {
        log.info("Activating trainee profile");

        Trainee trainee = authenticateTrainee(request);
        changeActiveStatus(trainee, true, "Trainee profile is already active");

        log.info("Trainee profile activated, userId={}", trainee.getId());
    }

    @Override
    @Transactional
    public void deactivate(AuthRequest request) {
        log.info("Deactivating trainee profile");

        Trainee trainee = authenticateTrainee(request);
        changeActiveStatus(trainee, false, "Trainee profile is already inactive");

        log.info("Trainee profile deactivated, userId={}", trainee.getId());
    }

    @Override
    @Transactional
    public void deleteByUsername(AuthRequest request) {
        log.info("Deleting trainee profile");

        Trainee trainee = authenticateTrainee(request);
        traineeDao.delete(trainee.getId());

        log.info("Trainee profile deleted, username={}", request.username());
    }

    @Override
    @Transactional
    public List<TrainerSummaryResponse> updateTrainers(UpdateTraineeTrainersRequest request) {
        requireNonNull(request, "Update trainee trainers request must not be null");
        requireNonNull(request.trainerUsernames(), "Trainer usernames must not be null");
        log.info("Updating trainee trainers list");

        Set<String> uniqueTrainerUsernames = normalizeTrainerUsernames(request.trainerUsernames());
        Trainee trainee = authenticationService.authenticateTrainee(request.username(), request.password());
        List<Trainer> trainers = uniqueTrainerUsernames.stream()
                .map(trainerUsername -> trainerDao.findByUsername(trainerUsername)
                        .orElseThrow(() -> new EntityNotFoundException("Trainer profile not found")))
                .toList();

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);
        traineeDao.save(trainee);

        log.info("Trainee trainers list updated, userId={}", trainee.getId());
        return trainers.stream()
                .map(trainerMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional
    public TraineeProfileResponse update(UpdateTraineeRequest request) {
        requireNonNull(request, "Update trainee request must not be null");
        validateNameFields(request.firstName(), request.lastName());
        log.info("Updating trainee profile");

        Trainee authenticatedTrainee = authenticationService.authenticateTrainee(request.username(), request.password());
        traineeMapper.updateFromRequest(request, authenticatedTrainee);
        traineeDao.save(authenticatedTrainee);

        log.info("Trainee profile updated, userId={}", authenticatedTrainee.getId());
        return traineeMapper.toProfileResponse(authenticatedTrainee);
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

    private static void validateCreateRequest(CreateTraineeRequest request) {
        validateNameFields(request.firstName(), request.lastName());
    }

    private static void validateNameFields(String firstName, String lastName) {
        requireNonBlank(firstName, "First name must not be blank");
        requireNonBlank(lastName, "Last name must not be blank");
    }

    private static Set<String> normalizeTrainerUsernames(List<String> trainerUsernames) {
        Set<String> uniqueTrainerUsernames = new LinkedHashSet<>();
        for (String trainerUsername : trainerUsernames) {
            requireNonBlank(trainerUsername, "Trainer username must not be blank");
            uniqueTrainerUsernames.add(trainerUsername);
        }
        return uniqueTrainerUsernames;
    }

    private Trainee authenticateTrainee(AuthRequest request) {
        requireNonNull(request, "Authentication request must not be null");
        return authenticationService.authenticateTrainee(request.username(), request.password());
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
