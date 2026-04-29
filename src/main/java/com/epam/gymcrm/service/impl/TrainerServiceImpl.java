package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerDao trainerDao;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private static final String MESSAGE = "Trainer id must not be null";


    @Override
    public void create(Trainer trainer) {
        requireNonNull(trainer, "Trainer must not be null");
        log.info("Creating trainer profile");
        trainer.setUsername(usernameGenerator.generate(
                trainer.getFirstName(),
                trainer.getLastName(),
                trainerDao.findAll().stream()
                        .map(Trainer::getUsername)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        ));

        trainer.setPassword(passwordGenerator.generate());
        trainerDao.save(trainer);
        log.info("Trainer profile created, userId={}", trainer.getUserId());
    }

    @Override
    public void update(Trainer trainer) {
        requireNonNull(trainer, "Trainer must not be null");
        requireNonNull(trainer.getUserId(), MESSAGE);
        log.info("Updating trainer profile, userId={}", trainer.getUserId());
        trainerDao.findById(trainer.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer profile not found"));
        trainerDao.save(trainer);
        log.info("Trainer profile updated, userId={}", trainer.getUserId());
    }

    @Override
    public Trainer findById(Long id) {
        requireNonNull(id, MESSAGE);
            return trainerDao.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer profile not found"));
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
