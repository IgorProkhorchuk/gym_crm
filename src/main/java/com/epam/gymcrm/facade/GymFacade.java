package com.epam.gymcrm.facade;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public void createTrainee(Trainee trainee) {
        traineeService.create(trainee);
    }

    public Trainee findTraineeById(Long id) {
        return traineeService.findById(id);
    }

    public Trainee getTraineeProfile(String username, String password) {
        return traineeService.getProfile(username, password);
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

    public Trainer getTrainerProfile(String username, String password) {
        return trainerService.getProfile(username, password);
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
