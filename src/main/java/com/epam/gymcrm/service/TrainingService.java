package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrainingService {
    private TrainingDao trainingDao;

    @Autowired
    public void  setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    public void create(Training training) {
        trainingDao.save(training);
    }

    public Optional<Training> findById(Long id) {
        return trainingDao.findById(id);
    }
}
