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
        log.info("Creating trainer profile for {} {}", trainer.getFirstName(), trainer.getLastName());
        trainer.setUsername(generateUsername(trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(passwordGenerator.generate());
        trainerDao.save(trainer);
        log.info("Created trainer profile with username {}", trainer.getUsername());
    }

    @Override
    public void update(Trainer trainer) {
        log.info("Updating trainer profile with id {}", trainer.getUserId());
        trainerDao.save(trainer);
        log.info("Updated trainer profile with id {}", trainer.getUserId());
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        log.debug("Searching trainer profile by id {}", id);
        Optional<Trainer> trainer = trainerDao.findById(id);
        log.debug("Trainer profile lookup for id {} returned {}", id, trainer.isPresent() ? "a result" : "no result");
        return trainer;
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
