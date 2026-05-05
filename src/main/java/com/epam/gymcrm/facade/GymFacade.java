package com.epam.gymcrm.facade;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.AddTrainingRequest;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public void activateTrainee(String username, String password) {
        traineeService.activate(username, password);
    }

    public void deactivateTrainee(String username, String password) {
        traineeService.deactivate(username, password);
    }

    public void deleteTraineeByUsername(String username, String password) {
        traineeService.deleteByUsername(username, password);
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

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    public void activateTrainer(String username, String password) {
        trainerService.activate(username, password);
    }

    public void deactivateTrainer(String username, String password) {
        trainerService.deactivate(username, password);
    }

    public List<Trainer> getUnassignedTrainers(String traineeUsername, String traineePassword) {
        return trainerService.getUnassignedTrainers(traineeUsername, traineePassword);
    }

    public void updateTrainer(Trainer trainer) {
        trainerService.update(trainer);
    }

    public void createTraining(Training training) {
        trainingService.create(training);
    }

    public void addTraining(String traineeUsername, String traineePassword, AddTrainingRequest request) {
        trainingService.addTraining(traineeUsername, traineePassword, request);
    }

    public Training findTrainingById(Long id) {
        return trainingService.findById(id);
    }

    public List<Training> getTraineeTrainings(
            String username,
            String password,
            TraineeTrainingCriteria criteria
    ) {
        return trainingService.getTraineeTrainings(username, password, criteria);
    }

    public List<Training> getTrainerTrainings(
            String username,
            String password,
            TrainerTrainingCriteria criteria
    ) {
        return trainingService.getTrainerTrainings(username, password, criteria);
    }
}
