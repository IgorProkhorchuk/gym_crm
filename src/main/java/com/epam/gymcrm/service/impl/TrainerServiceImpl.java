package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TrainerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrainerServiceImpl implements TrainerService {

    private TrainerDao trainerDao;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public void create(Trainer trainer) {
        log.info("Creating trainer profile");
        try {
            trainer.setUsername(generateUsername(trainer.getFirstName(), trainer.getLastName()));
            trainer.setPassword(passwordGenerator.generate());
            trainerDao.save(trainer);
            log.info("Trainer profile created, userId={}", trainer.getUserId());
        } catch (RuntimeException e) {
            log.error("Failed to create trainer profile, userId={}", trainer.getUserId(), e);
            throw e;
        }
    }

    @Override
    public void update(Trainer trainer) {
        log.info("Updating trainer profile, userId={}", trainer.getUserId());
        try {
            trainerDao.save(trainer);
            log.info("Trainer profile updated, userId={}", trainer.getUserId());
        } catch (RuntimeException e) {
            log.error("Failed to update trainer profile, userId={}", trainer.getUserId(), e);
            throw e;
        }
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        try {
            return trainerDao.findById(id);
        } catch (RuntimeException e) {
            log.error("Failed to find trainer profile, userId={}", id, e);
            throw e;
        }
    }


    private String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;

        Set<String> existingUsernames = trainerDao.findAll().stream()
                .map(Trainer::getUsername)
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
