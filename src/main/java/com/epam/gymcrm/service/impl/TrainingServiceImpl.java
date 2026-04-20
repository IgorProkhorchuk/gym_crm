package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class TrainingServiceImpl implements TrainingService {

    private TrainingDao trainingDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public void create(Training training) {
        log.info("Creating training profile with id {}", training.getTrainingId());
        trainingDao.save(training);
        log.info("Created training profile with id {}", training.getTrainingId());
    }

    @Override
    public Optional<Training> findById(Long id) {
        log.debug("Searching training profile by id {}", id);
        Optional<Training> training = trainingDao.findById(id);
        log.debug("Training profile lookup for id {} returned {}", id, training.isPresent() ? "a result" : "no result");
        return training;
    }
}
