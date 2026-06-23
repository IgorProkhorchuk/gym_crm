package com.epam.gymcrm.facade;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainingTypeResponse;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import com.epam.gymcrm.service.TrainingTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GymFacade {

  private final TraineeService traineeService;
  private final TrainerService trainerService;
  private final TrainingService trainingService;
  private final TrainingTypeService trainingTypeService;

  public UsernamePasswordResponse createTrainee(CreateTraineeRequest request) {
    return traineeService.create(request);
  }

  public TraineeProfileResponse getTraineeProfile(AuthRequest request) {
    return traineeService.getProfile(request);
  }

  public void changeTraineePassword(ChangePasswordRequest request) {
    traineeService.changePassword(request);
  }

  public void switchTraineeActiveStatus(AuthRequest request) {
    traineeService.switchActiveStatus(request);
  }

  public void deleteTraineeByUsername(AuthRequest request) {
    traineeService.deleteByUsername(request);
  }

  public List<TrainerSummaryResponse> updateTraineeTrainers(UpdateTraineeTrainersRequest request) {
    return traineeService.updateTrainers(request);
  }

  public TraineeProfileResponse updateTrainee(UpdateTraineeRequest request) {
    return traineeService.update(request);
  }

  public UsernamePasswordResponse createTrainer(CreateTrainerRequest request) {
    return trainerService.create(request);
  }

  public TrainerProfileResponse getTrainerProfile(AuthRequest request) {
    return trainerService.getProfile(request);
  }

  public void changeTrainerPassword(ChangePasswordRequest request) {
    trainerService.changePassword(request);
  }

  public void switchTrainerActiveStatus(AuthRequest request) {
    trainerService.switchActiveStatus(request);
  }

  public List<TrainerSummaryResponse> getUnassignedTrainers(AuthRequest request) {
    return trainerService.getUnassignedTrainers(request);
  }

  public TrainerProfileResponse updateTrainer(UpdateTrainerRequest request) {
    return trainerService.update(request);
  }

  public void addTraining(AddTrainingRequest request) {
    trainingService.addTraining(request);
  }

  public void deleteTraining(Long trainingId) {
    trainingService.deleteTraining(trainingId);
  }

  public List<TraineeTrainingResponse> getTraineeTrainings(TraineeTrainingsRequest request) {
    return trainingService.getTraineeTrainings(request);
  }

  public List<TrainerTrainingResponse> getTrainerTrainings(TrainerTrainingsRequest request) {
    return trainingService.getTrainerTrainings(request);
  }

  public List<TrainingTypeResponse> getTrainingTypes() {
    return trainingTypeService.getTrainingTypes();
  }
}
