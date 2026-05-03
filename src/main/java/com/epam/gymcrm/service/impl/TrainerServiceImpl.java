package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.service.PasswordGenerator;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerDao trainerDao;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private static final String TRAINER_ID_NULL_ERROR = "Trainer id must not be null";


    @Override
    public void create(Trainer trainer) {
        requireNonNull(trainer, "Trainer must not be null");
        requireNonNull(trainer.getUser(), "Trainer username must not be null");

        User user = trainer.getUser();

        log.info("Creating trainer profile");
        user.setUsername(usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName(),
                trainerDao.findAll().stream()
                        .map(Trainer::getUser)
                        .filter(Objects::nonNull)
                        .map(User::getUsername)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        ));

        user.setPassword(passwordGenerator.generate());
        trainerDao.save(trainer);
        log.info("Trainer profile created, Id={}, userId={}", trainer.getId(), user.getUserId());
    }

    @Override
    public void update(Trainer trainer) {
        requireNonNull(trainer, "Trainer must not be null");
        requireNonNull(trainer.getId(), TRAINER_ID_NULL_ERROR);
        log.info("Updating trainer profile, userId={}", trainer.getId());
        trainerDao.findById(trainer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer profile not found"));
        trainerDao.save(trainer);
        log.info("Trainer profile updated, userId={}", trainer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer findById(Long id) {
        requireNonNull(id, TRAINER_ID_NULL_ERROR);
            return trainerDao.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer profile not found"));
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
