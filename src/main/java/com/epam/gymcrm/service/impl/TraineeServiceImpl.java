package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TraineeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
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
        log.info("Creating trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());
        trainee.setUsername(generateUsername(trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(passwordGenerator.generate());
        traineeDao.save(trainee);
        log.info("Created trainee profile with username {}", trainee.getUsername());
    }

    @Override
    public void update(Trainee trainee) {
        log.info("Updating trainee profile with id {}", trainee.getUserId());
        traineeDao.save(trainee);
        log.info("Updated trainee profile with id {}", trainee.getUserId());
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting trainee profile with id {}", id);
        traineeDao.delete(id);
        log.info("Deleted trainee profile with id {}", id);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        log.debug("Searching trainee profile by id {}", id);
        Optional<Trainee> trainee = traineeDao.findById(id);
        log.debug("Trainee profile lookup for id {} returned {}", id, trainee.isPresent() ? "a result" : "no result");
        return trainee;
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
}
