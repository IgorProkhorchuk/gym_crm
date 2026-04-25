package com.epam.gymcrm.facade;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.springframework.stereotype.Component;

@Component
public class GymFacade {

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public void createTrainee(Trainee trainee) {
        traineeService.create(trainee);
    }

    public Trainee findTraineeById(Long id) {
        return traineeService.findById(id);
    }

    public void updateTrainee(Trainee trainee) {
        traineeService.update(trainee);
    }

    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    public void createTrainer(Trainer trainer) {
        trainerService.create(trainer);
    }

    public Trainer findTrainerById(Long id) {
        return trainerService.findById(id);
    }

    public void updateTrainer(Trainer trainer) {
        trainerService.update(trainer);
    }

    public void createTraining(Training training) {
        trainingService.create(training);
    }

    public Training findTrainingById(Long id) {
        return trainingService.findById(id);
    }
}
