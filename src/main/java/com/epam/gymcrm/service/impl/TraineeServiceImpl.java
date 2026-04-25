package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TraineeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TraineeServiceImpl implements TraineeService {

    private TraineeDao traineeDao;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public void create(Trainee trainee) {
        requireNonNull(trainee, "Trainee must not be null");
        log.info("Creating trainee profile");
        try {
            trainee.setUsername(generateUsername(trainee.getFirstName(), trainee.getLastName()));
            trainee.setPassword(passwordGenerator.generate());
            traineeDao.save(trainee);
            log.info("Trainee profile created, userId={}", trainee.getUserId());
        } catch (RuntimeException e) {
            log.error("Failed to create trainee profile, userId={}", trainee.getUserId(), e);
            throw e;
        }
    }

    @Override
    public void update(Trainee trainee) {
        requireNonNull(trainee, "Trainee must not be null");
        log.info("Updating trainee profile, userId={}", trainee.getUserId());
        try {
            traineeDao.save(trainee);
            log.info("Trainee profile updated, userId={}", trainee.getUserId());
        } catch (RuntimeException e) {
            log.error("Failed to update trainee profile, userId={}", trainee.getUserId(), e);
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        requireNonNull(id, "Trainee id must not be null");
        log.info("Deleting trainee profile, userId={}", id);
        try {
            traineeDao.delete(id);
            log.info("Trainee profile deleted, userId={}", id);
        } catch (RuntimeException e) {
            log.error("Failed to delete trainee profile, userId={}", id, e);
            throw e;
        }
    }

    @Override
    public Trainee findById(Long id) {
        requireNonNull(id, "Trainee id must not be null");
        try {
            return traineeDao.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Trainee profile not found"));
        } catch (RuntimeException e) {
            log.error("Failed to find trainee profile, userId={}", id, e);
            throw e;
        }
    }


    private String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;

        Set<String> existingUsernames = traineeDao.findAll().stream()
                .map(Trainee::getUsername)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!existingUsernames.contains(baseUsername)) {
            return baseUsername;
        }

        int suffix = 1;
        while (existingUsernames.contains(baseUsername + suffix)) {
            suffix++;
        }

        return baseUsername + suffix;
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
