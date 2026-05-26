package com.epam.gymcrm.service;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainingMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.service.impl.TrainingServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

  @InjectMocks private TrainingServiceImpl trainingService;

  @Mock private TrainingDao trainingDao;

  @Mock private TrainerDao trainerDao;

  @Mock private TrainingTypeDao trainingTypeDao;

  @Mock private AuthenticationService authenticationService;

  @Mock private TrainingMapper trainingMapper;

  private AddTrainingRequest addTrainingRequest;

  @BeforeEach
  void setUp() {
    addTrainingRequest =
        new AddTrainingRequest(
            "Training.Trainee",
            "password",
            "Training.Trainer",
            "Yoga Basics",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            60);
  }

  @Test
  void addTrainingShouldAuthenticateTraineeAndSaveTraining() {
    Trainee trainee = trainee("Training", "Trainee", "Training.Trainee");
    Trainer trainer = trainer("Training", "Trainer", "Training.Trainer");
    TrainingType trainingType = trainingType("Yoga");
    Training training =
        Training.builder()
            .trainingName("Yoga Basics")
            .trainingDate(LocalDate.of(2026, 5, 3))
            .trainingDuration(60)
            .build();
    when(authenticationService.authenticateTrainee("Training.Trainee", "password"))
        .thenReturn(trainee);
    when(trainerDao.findByUsername("Training.Trainer")).thenReturn(Optional.of(trainer));
    when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(trainingType));
    when(trainingMapper.toEntity(addTrainingRequest)).thenReturn(training);

    trainingService.addTraining(addTrainingRequest);

    assertAll(
        () -> verify(authenticationService).authenticateTrainee("Training.Trainee", "password"),
        () -> verify(trainerDao).findByUsername("Training.Trainer"),
        () -> verify(trainingTypeDao).findByName("Yoga"),
        () -> verify(trainingMapper).toEntity(addTrainingRequest),
        () -> verify(trainingDao).save(training),
        () -> assertThat(training.getTrainee()).isSameAs(trainee),
        () -> assertThat(training.getTrainer()).isSameAs(trainer),
        () -> assertThat(training.getTrainingType()).isSameAs(trainingType),
        () -> assertThat(training.getTrainingName()).isEqualTo("Yoga Basics"),
        () -> assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2026, 5, 3)),
        () -> assertThat(training.getTrainingDuration()).isEqualTo(60));
  }

  @Test
  void addTrainingShouldThrowEntityNotFoundExceptionWhenTrainerDoesNotExist() {
    Trainee trainee = trainee("Training", "Trainee", "Training.Trainee");
    when(authenticationService.authenticateTrainee("Training.Trainee", "password"))
        .thenReturn(trainee);
    when(trainerDao.findByUsername("Training.Trainer")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainingService.addTraining(addTrainingRequest))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Trainer profile not found");
  }

  @Test
  void addTrainingShouldThrowEntityNotFoundExceptionWhenTrainingTypeDoesNotExist() {
    Trainee trainee = trainee("Training", "Trainee", "Training.Trainee");
    Trainer trainer = trainer("Training", "Trainer", "Training.Trainer");
    when(authenticationService.authenticateTrainee("Training.Trainee", "password"))
        .thenReturn(trainee);
    when(trainerDao.findByUsername("Training.Trainer")).thenReturn(Optional.of(trainer));
    when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainingService.addTraining(addTrainingRequest))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Training type not found");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
    assertThatThrownBy(() -> trainingService.addTraining(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Training request must not be null");

    verifyNoInteractions(
        authenticationService, trainerDao, trainingTypeDao, trainingDao, trainingMapper);
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTraineeUsernameIsBlank() {
    AddTrainingRequest request =
        addTrainingRequest(
            " ",
            "password",
            "Training.Trainer",
            "Yoga Basics",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            60);

    assertThatThrownBy(() -> trainingService.addTraining(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainee username must not be blank");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTraineePasswordIsBlank() {
    AddTrainingRequest request =
        addTrainingRequest(
            "Training.Trainee",
            " ",
            "Training.Trainer",
            "Yoga Basics",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            60);

    assertThatThrownBy(() -> trainingService.addTraining(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainee password must not be blank");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainerUsernameIsBlank() {
    AddTrainingRequest request =
        addTrainingRequest(
            "Training.Trainee",
            "password",
            " ",
            "Yoga Basics",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            60);

    assertThatThrownBy(() -> trainingService.addTraining(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainer username must not be blank");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainerUsernameIsNull() {
    AddTrainingRequest request =
        addTrainingRequest(
            "Training.Trainee",
            "password",
            null,
            "Yoga Basics",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            60);

    assertThatThrownBy(() -> trainingService.addTraining(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainer username must not be blank");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingNameIsBlank() {
    AddTrainingRequest request =
        addTrainingRequest(
            "Training.Trainee",
            "password",
            "Training.Trainer",
            " ",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            60);

    assertThatThrownBy(() -> trainingService.addTraining(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Training name must not be blank");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingTypeIsBlank() {
    AddTrainingRequest request =
        addTrainingRequest(
            "Training.Trainee",
            "password",
            "Training.Trainer",
            "Yoga Basics",
            " ",
            LocalDate.of(2026, 5, 3),
            60);

    assertThatThrownBy(() -> trainingService.addTraining(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Training type must not be blank");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingDateIsNull() {
    AddTrainingRequest request =
        addTrainingRequest(
            "Training.Trainee", "password", "Training.Trainer", "Yoga Basics", "Yoga", null, 60);

    assertThatThrownBy(() -> trainingService.addTraining(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Training date must not be null");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingDurationIsNull() {
    AddTrainingRequest request =
        addTrainingRequest(
            "Training.Trainee",
            "password",
            "Training.Trainer",
            "Yoga Basics",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            null);

    assertThatThrownBy(() -> trainingService.addTraining(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Training duration must be positive");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingDurationIsZero() {
    AddTrainingRequest request =
        addTrainingRequest(
            "Training.Trainee",
            "password",
            "Training.Trainer",
            "Yoga Basics",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            0);

    assertThatThrownBy(() -> trainingService.addTraining(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Training duration must be positive");
  }

  @Test
  void getTraineeTrainingsShouldAuthenticateTraineeAndReturnMappedTrainings() {
    TraineeTrainingsRequest request =
        new TraineeTrainingsRequest(
            "Training.Trainee",
            "password",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            "Coach",
            "Yoga",
            PageRequest.firstPage());
    TraineeTrainingCriteria criteria =
        new TraineeTrainingCriteria(
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "Coach", "Yoga");
    Training training = validTraining();
    TraineeTrainingResponse response = traineeTrainingResponse();
    when(trainingMapper.toCriteria(request)).thenReturn(criteria);
    when(trainingDao.findByTraineeUsernameAndCriteria(
            "Training.Trainee", criteria, PageRequest.firstPage()))
        .thenReturn(List.of(training));
    when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(response);

    List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> verify(authenticationService).authenticateTrainee("Training.Trainee", "password"),
        () -> verify(trainingMapper).toCriteria(request),
        () ->
            verify(trainingDao)
                .findByTraineeUsernameAndCriteria(
                    "Training.Trainee", criteria, PageRequest.firstPage()),
        () -> verify(trainingMapper).toTraineeTrainingResponse(training));
  }

  @Test
  void getTraineeTrainingsShouldUseEmptyCriteriaWhenMapperReturnsNull() {
    TraineeTrainingsRequest request =
        new TraineeTrainingsRequest("Training.Trainee", "password", null, null, null, null, null);
    Training training = validTraining();
    TraineeTrainingResponse response = traineeTrainingResponse();
    when(trainingMapper.toCriteria(request)).thenReturn(null);
    when(trainingDao.findByTraineeUsernameAndCriteria(
            "Training.Trainee", TraineeTrainingCriteria.empty(), PageRequest.firstPage()))
        .thenReturn(List.of(training));
    when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(response);

    List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> verify(authenticationService).authenticateTrainee("Training.Trainee", "password"),
        () ->
            verify(trainingDao)
                .findByTraineeUsernameAndCriteria(
                    "Training.Trainee", TraineeTrainingCriteria.empty(), PageRequest.firstPage()));
  }

  @Test
  void getTrainerTrainingsShouldAuthenticateTrainerAndReturnMappedTrainings() {
    TrainerTrainingsRequest request =
        new TrainerTrainingsRequest(
            "Training.Trainer",
            "password",
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28),
            "Trainee",
            PageRequest.firstPage());
    TrainerTrainingCriteria criteria =
        new TrainerTrainingCriteria(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), "Trainee");
    Training training = validTraining();
    TrainerTrainingResponse response = trainerTrainingResponse();
    when(trainingMapper.toCriteria(request)).thenReturn(criteria);
    when(trainingDao.findByTrainerUsernameAndCriteria(
            "Training.Trainer", criteria, PageRequest.firstPage()))
        .thenReturn(List.of(training));
    when(trainingMapper.toTrainerTrainingResponse(training)).thenReturn(response);

    List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> verify(authenticationService).authenticateTrainer("Training.Trainer", "password"),
        () -> verify(trainingMapper).toCriteria(request),
        () ->
            verify(trainingDao)
                .findByTrainerUsernameAndCriteria(
                    "Training.Trainer", criteria, PageRequest.firstPage()),
        () -> verify(trainingMapper).toTrainerTrainingResponse(training));
  }

  @Test
  void getTrainerTrainingsShouldUseEmptyCriteriaWhenMapperReturnsNull() {
    TrainerTrainingsRequest request =
        new TrainerTrainingsRequest("Training.Trainer", "password", null, null, null, null);
    Training training = validTraining();
    TrainerTrainingResponse response = trainerTrainingResponse();
    when(trainingMapper.toCriteria(request)).thenReturn(null);
    when(trainingDao.findByTrainerUsernameAndCriteria(
            "Training.Trainer", TrainerTrainingCriteria.empty(), PageRequest.firstPage()))
        .thenReturn(List.of(training));
    when(trainingMapper.toTrainerTrainingResponse(training)).thenReturn(response);

    List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> verify(authenticationService).authenticateTrainer("Training.Trainer", "password"),
        () ->
            verify(trainingDao)
                .findByTrainerUsernameAndCriteria(
                    "Training.Trainer", TrainerTrainingCriteria.empty(), PageRequest.firstPage()));
  }

  private static AddTrainingRequest addTrainingRequest(
      String traineeUsername,
      String traineePassword,
      String trainerUsername,
      String trainingName,
      String trainingTypeName,
      LocalDate trainingDate,
      Integer trainingDuration) {
    return new AddTrainingRequest(
        traineeUsername,
        traineePassword,
        trainerUsername,
        trainingName,
        trainingTypeName,
        trainingDate,
        trainingDuration);
  }

  private static Training validTraining() {
    return training(
        trainee("Training", "Trainee", "Training.Trainee"),
        trainer("Training", "Trainer", "Training.Trainer"),
        trainingType("Yoga"));
  }

  private static TraineeTrainingResponse traineeTrainingResponse() {
    return new TraineeTrainingResponse(
        "Yoga Basics", "Yoga", LocalDate.of(2026, 5, 3), 60, "Training Trainer");
  }

  private static TrainerTrainingResponse trainerTrainingResponse() {
    return new TrainerTrainingResponse(
        "Yoga Basics", "Yoga", LocalDate.of(2026, 5, 3), 60, "Training Trainee");
  }
}
