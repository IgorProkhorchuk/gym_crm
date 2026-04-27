package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.UsernameGenerator;
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
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private static final String MESSAGE = "Trainee id must not be null";


    @Override
    public void create(Trainee trainee) {
        requireNonNull(trainee, "Trainee must not be null");
        log.info("Creating trainee profile");
        trainee.setUsername(usernameGenerator.generate(
                trainee.getFirstName(),
                trainee.getLastName(),
                traineeDao.findAll().stream()
                        .map(Trainee::getUsername)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        ));
        trainee.setPassword(passwordGenerator.generate());
        traineeDao.save(trainee);
        log.info("Trainee profile created, userId={}", trainee.getUserId());
    }

    @Override
    public void update(Trainee trainee) {
        requireNonNull(trainee, "Trainee must not be null");
        requireNonNull(trainee.getUserId(), MESSAGE);
        log.info("Updating trainee profile, userId={}", trainee.getUserId());
        traineeDao.findById(trainee.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee profile not found"));
        traineeDao.save(trainee);
        log.info("Trainee profile updated, userId={}", trainee.getUserId());
    }

    @Override
    public void delete(Long id) {
        requireNonNull(id, MESSAGE);
        log.info("Deleting trainee profile, userId={}", id);

        traineeDao.delete(id);

        log.info("Trainee profile deleted, userId={}", id);
    }

    @Override
    public Trainee findById(Long id) {
        requireNonNull(id, MESSAGE);

        return traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee profile not found"));
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
