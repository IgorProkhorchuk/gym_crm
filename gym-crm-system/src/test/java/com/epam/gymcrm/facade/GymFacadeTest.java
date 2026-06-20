package com.epam.gymcrm.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.PageRequest;
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
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

  @InjectMocks private GymFacade gymFacade;

  @Mock private TraineeService traineeService;

  @Mock private TrainerService trainerService;

  @Mock private TrainingService trainingService;

  @Mock private TrainingTypeService trainingTypeService;

  @Test
  void createTraineeShouldDelegateToTraineeService() {
    CreateTraineeRequest request =
        new CreateTraineeRequest("John", "Doe", LocalDate.of(1995, 1, 10), "Main Street, 123");
    UsernamePasswordResponse response = new UsernamePasswordResponse("John.Doe", "Passw0rd12");
    when(traineeService.create(request)).thenReturn(response);

    UsernamePasswordResponse result = gymFacade.createTrainee(request);

    assertAll(
        () -> assertThat(result).isSameAs(response), () -> verify(traineeService).create(request));
  }

  @Test
  void getTraineeProfileShouldReturnTraineeProfileFromService() {
    AuthRequest request = new AuthRequest("John.Doe");
    TraineeProfileResponse response =
        new TraineeProfileResponse(
            "John.Doe",
            "John",
            "Doe",
            true,
            LocalDate.of(1995, 1, 10),
            "Main Street, 123",
            List.of());
    when(traineeService.getProfile(request)).thenReturn(response);

    TraineeProfileResponse result = gymFacade.getTraineeProfile(request);

    assertAll(
        () -> assertThat(result).isSameAs(response),
        () -> verify(traineeService).getProfile(request));
  }

  @Test
  void changeTraineePasswordShouldDelegateToTraineeService() {
    ChangePasswordRequest request =
        new ChangePasswordRequest("John.Doe", "old-password", "new-password");

    gymFacade.changeTraineePassword(request);

    verify(traineeService).changePassword(request);
  }

  @Test
  void switchTraineeActiveStatusShouldDelegateToTraineeService() {
    AuthRequest request = new AuthRequest("John.Doe");

    gymFacade.switchTraineeActiveStatus(request);

    verify(traineeService).switchActiveStatus(request);
  }

  @Test
  void deleteTraineeByUsernameShouldDelegateToTraineeService() {
    AuthRequest request = new AuthRequest("John.Doe");

    gymFacade.deleteTraineeByUsername(request);

    verify(traineeService).deleteByUsername(request);
  }

  @Test
  void updateTraineeTrainersShouldReturnTrainersFromService() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest(
            "John.Doe", List.of("First.Trainer", "Second.Trainer"));
    List<TrainerSummaryResponse> trainers =
        List.of(
            new TrainerSummaryResponse("First.Trainer", "First", "Trainer", "Fitness"),
            new TrainerSummaryResponse("Second.Trainer", "Second", "Trainer", "Fitness"));
    when(traineeService.updateTrainers(request)).thenReturn(trainers);

    List<TrainerSummaryResponse> result = gymFacade.updateTraineeTrainers(request);

    assertAll(
        () -> assertThat(result).isSameAs(trainers),
        () -> verify(traineeService).updateTrainers(request));
  }

  @Test
  void updateTraineeShouldDelegateToTraineeService() {
    UpdateTraineeRequest request =
        new UpdateTraineeRequest(
            "John.Doe",
            "John",
            "Doe",
            LocalDate.of(1995, 1, 10),
            "Main Street, 123",
            true);
    TraineeProfileResponse response =
        new TraineeProfileResponse(
            "John.Doe",
            "John",
            "Doe",
            true,
            LocalDate.of(1995, 1, 10),
            "Main Street, 123",
            List.of());
    when(traineeService.update(request)).thenReturn(response);

    TraineeProfileResponse result = gymFacade.updateTrainee(request);

    assertAll(
        () -> assertThat(result).isSameAs(response), () -> verify(traineeService).update(request));
  }

  @Test
  void createTrainerShouldDelegateToTrainerService() {
    CreateTrainerRequest request = new CreateTrainerRequest("Mike", "Stone", "Fitness");
    UsernamePasswordResponse response = new UsernamePasswordResponse("Mike.Stone", "Passw0rd12");
    when(trainerService.create(request)).thenReturn(response);

    UsernamePasswordResponse result = gymFacade.createTrainer(request);

    assertAll(
        () -> assertThat(result).isSameAs(response), () -> verify(trainerService).create(request));
  }

  @Test
  void getTrainerProfileShouldReturnTrainerProfileFromService() {
    AuthRequest request = new AuthRequest("Mike.Stone");
    TrainerProfileResponse response =
        new TrainerProfileResponse("Mike.Stone", "Mike", "Stone", true, "Fitness");
    when(trainerService.getProfile(request)).thenReturn(response);

    TrainerProfileResponse result = gymFacade.getTrainerProfile(request);

    assertAll(
        () -> assertThat(result).isSameAs(response),
        () -> verify(trainerService).getProfile(request));
  }

  @Test
  void changeTrainerPasswordShouldDelegateToTrainerService() {
    ChangePasswordRequest request =
        new ChangePasswordRequest("Mike.Stone", "old-password", "new-password");

    gymFacade.changeTrainerPassword(request);

    verify(trainerService).changePassword(request);
  }

  @Test
  void switchTrainerActiveStatusShouldDelegateToTrainerService() {
    AuthRequest request = new AuthRequest("Mike.Stone");

    gymFacade.switchTrainerActiveStatus(request);

    verify(trainerService).switchActiveStatus(request);
  }

  @Test
  void getUnassignedTrainersShouldReturnTrainersFromService() {
    AuthRequest request = new AuthRequest("John.Doe");
    List<TrainerSummaryResponse> trainers =
        List.of(new TrainerSummaryResponse("Available.Trainer", "Available", "Trainer", "Fitness"));
    when(trainerService.getUnassignedTrainers(request)).thenReturn(trainers);

    List<TrainerSummaryResponse> result = gymFacade.getUnassignedTrainers(request);

    assertAll(
        () -> assertThat(result).isSameAs(trainers),
        () -> verify(trainerService).getUnassignedTrainers(request));
  }

  @Test
  void updateTrainerShouldDelegateToTrainerService() {
    UpdateTrainerRequest request =
        new UpdateTrainerRequest("Mike.Stone", "Mike", "Stone", "Fitness", true);
    TrainerProfileResponse response =
        new TrainerProfileResponse("Mike.Stone", "Mike", "Stone", true, "Fitness");
    when(trainerService.update(request)).thenReturn(response);

    TrainerProfileResponse result = gymFacade.updateTrainer(request);

    assertAll(
        () -> assertThat(result).isSameAs(response), () -> verify(trainerService).update(request));
  }

  @Test
  void addTrainingShouldDelegateToTrainingService() {
    AddTrainingRequest request =
        new AddTrainingRequest(
            "John.Doe",
            "Mike.Stone",
            "Yoga Basics",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            60);

    gymFacade.addTraining(request);

    verify(trainingService).addTraining(request);
  }

  @Test
  void deleteTrainingShouldDelegateToTrainingService() {
    gymFacade.deleteTraining(10L);

    verify(trainingService).deleteTraining(10L);
  }

  @Test
  void getTraineeTrainingsShouldReturnTrainingResponsesFromService() {
    TraineeTrainingsRequest request =
        new TraineeTrainingsRequest(
            "John.Doe",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            "Mike",
            "Yoga",
            PageRequest.firstPage());
    List<TraineeTrainingResponse> trainings = List.of(traineeTrainingResponse());
    when(trainingService.getTraineeTrainings(request)).thenReturn(trainings);

    List<TraineeTrainingResponse> result = gymFacade.getTraineeTrainings(request);

    assertAll(
        () -> assertThat(result).isSameAs(trainings),
        () -> verify(trainingService).getTraineeTrainings(request));
  }

  @Test
  void getTrainerTrainingsShouldReturnTrainingResponsesFromService() {
    TrainerTrainingsRequest request =
        new TrainerTrainingsRequest(
            "Mike.Stone",
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28),
            "John",
            PageRequest.firstPage());
    List<TrainerTrainingResponse> trainings = List.of(trainerTrainingResponse());
    when(trainingService.getTrainerTrainings(request)).thenReturn(trainings);

    List<TrainerTrainingResponse> result = gymFacade.getTrainerTrainings(request);

    assertAll(
        () -> assertThat(result).isSameAs(trainings),
        () -> verify(trainingService).getTrainerTrainings(request));
  }

  @Test
  void getTrainingTypesShouldReturnTrainingTypesFromService() {
    List<TrainingTypeResponse> trainingTypes =
        List.of(new TrainingTypeResponse(1L, "Fitness"), new TrainingTypeResponse(2L, "Yoga"));
    when(trainingTypeService.getTrainingTypes()).thenReturn(trainingTypes);

    List<TrainingTypeResponse> result = gymFacade.getTrainingTypes();

    assertAll(
        () -> assertThat(result).isSameAs(trainingTypes),
        () -> verify(trainingTypeService).getTrainingTypes());
  }

  private static TraineeTrainingResponse traineeTrainingResponse() {
    return new TraineeTrainingResponse(
        "Yoga Basics", "Yoga", LocalDate.of(2026, 5, 3), 60, "Mike Stone");
  }

  private static TrainerTrainingResponse trainerTrainingResponse() {
    return new TrainerTrainingResponse(
        "Yoga Basics", "Yoga", LocalDate.of(2026, 5, 3), 60, "John Doe");
  }
}
