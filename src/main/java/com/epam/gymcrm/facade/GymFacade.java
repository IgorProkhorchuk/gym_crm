package com.epam.gymcrm.facade;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GymFacade {

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setTrainingService(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    public void createTrainee(Trainee trainee) {
        traineeService.create(trainee);
    }

    public Optional<Trainee> findTraineeById(Long id) {
        return traineeService.findById(id);
    }

    public void createTrainer(Trainer trainer) {
        trainerService.create(trainer);
    }

    public Optional<Trainer> findTrainerById(Long id) {
        return trainerService.findById(id);
    }

    public void createTraining(Training training) {
        trainingService.create(training);
    }

    public Optional<Training> findTrainingById(Long id) {
        return trainingService.findById(id);
    }
}
