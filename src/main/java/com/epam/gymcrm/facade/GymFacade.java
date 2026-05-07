package com.epam.gymcrm.facade;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
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

    public List<Trainer> updateTraineeTrainers(
            String traineeUsername,
            String traineePassword,
            List<String> trainerUsernames
    ) {
        return traineeService.updateTrainers(traineeUsername, traineePassword, trainerUsernames);
    }

    public void updateTrainee(String username, String password, Trainee trainee) {
        traineeService.update(username, password, trainee);
    }

    public void createTrainer(Trainer trainer) {
        trainerService.create(trainer);
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

    public void updateTrainer(String username, String password, Trainer trainer) {
        trainerService.update(username, password, trainer);
    }

    public void addTraining(String traineeUsername, String traineePassword, AddTrainingRequest request) {
        trainingService.addTraining(traineeUsername, traineePassword, request);
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
