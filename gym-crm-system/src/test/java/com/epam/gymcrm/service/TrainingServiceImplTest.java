package com.epam.gymcrm.service;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.client.workload.TrainerWorkloadActionType;
import com.epam.gymcrm.client.workload.TrainerWorkloadOutboxService;
import com.epam.gymcrm.client.workload.TrainerWorkloadRequest;
import com.epam.gymcrm.client.workload.TrainerWorkloadRequestFactory;
import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
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
import com.epam.gymcrm.monitoring.metrics.GymMetrics;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.service.impl.TrainingServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

  @InjectMocks private TrainingServiceImpl trainingService;

  @Mock private TrainingRepository trainingRepository;

  @Mock private TraineeRepository traineeRepository;

  @Mock private TrainerRepository trainerRepository;

  @Mock private TrainingTypeRepository trainingTypeRepository;

  @Mock private AuthenticationService authenticationService;

  @Mock private TrainingMapper trainingMapper;

  @Mock private GymMetrics gymMetrics;

  @Mock private TrainerWorkloadOutboxService trainerWorkloadOutboxService;

  @Mock private TrainerWorkloadRequestFactory trainerWorkloadRequestFactory;

  private AddTrainingRequest addTrainingRequest;
  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
    addTrainingRequest =
        new AddTrainingRequest(
            "Training.Trainee",
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
            .trainingId(10L)
            .build();
    TrainerWorkloadRequest workloadRequest = trainerWorkloadRequest(TrainerWorkloadActionType.ADD);
    when(traineeRepository.findByUsername("Training.Trainee")).thenReturn(Optional.of(trainee));
    when(trainerRepository.findByUsername("Training.Trainer")).thenReturn(Optional.of(trainer));
    when(trainingTypeRepository.findByName("Yoga")).thenReturn(Optional.of(trainingType));
    when(trainingMapper.toEntity(addTrainingRequest)).thenReturn(training);
    when(trainingRepository.save(training)).thenReturn(training);
    when(trainerWorkloadRequestFactory.fromTraining(training, TrainerWorkloadActionType.ADD))
        .thenReturn(workloadRequest);

    trainingService.addTraining(addTrainingRequest);

    assertAll(
        () -> verify(traineeRepository).findByUsername("Training.Trainee"),
        () -> verify(trainerRepository).findByUsername("Training.Trainer"),
        () -> verify(trainingTypeRepository).findByName("Yoga"),
        () -> verify(trainingMapper).toEntity(addTrainingRequest),
        () -> verify(trainingRepository).save(training),
        () -> verify(trainerWorkloadRequestFactory).fromTraining(training, TrainerWorkloadActionType.ADD),
        () -> verify(trainerWorkloadOutboxService).savePendingEvent(10L, workloadRequest),
        () -> verify(gymMetrics).recordTrainingCreationSucceeded(),
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
    when(traineeRepository.findByUsername("Training.Trainee")).thenReturn(Optional.of(trainee));
    when(trainerRepository.findByUsername("Training.Trainer")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainingService.addTraining(addTrainingRequest))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Trainer profile not found");

    verify(gymMetrics).recordTrainingCreationTrainerNotFound();
  }

  @Test
  void addTrainingShouldThrowEntityNotFoundExceptionWhenTrainingTypeDoesNotExist() {
    Trainee trainee = trainee("Training", "Trainee", "Training.Trainee");
    Trainer trainer = trainer("Training", "Trainer", "Training.Trainer");
    when(traineeRepository.findByUsername("Training.Trainee")).thenReturn(Optional.of(trainee));
    when(trainerRepository.findByUsername("Training.Trainer")).thenReturn(Optional.of(trainer));
    when(trainingTypeRepository.findByName("Yoga")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainingService.addTraining(addTrainingRequest))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Training type not found");

    verify(gymMetrics).recordTrainingCreationTrainingTypeNotFound();
  }

  @Test
  void addTrainingShouldRecordAuthFailureWhenTraineeProfileDoesNotExist() {
    when(traineeRepository.findByUsername("Training.Trainee")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainingService.addTraining(addTrainingRequest))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Trainee profile not found");

    verify(gymMetrics).recordTrainingCreationAuthFailed();
  }

  @Test
  void deleteTrainingShouldDeleteTrainingAndUpdateTrainerWorkload() {
    Training training = validTraining();
    TrainerWorkloadRequest workloadRequest = trainerWorkloadRequest(TrainerWorkloadActionType.DELETE);
    when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));
    when(trainerWorkloadRequestFactory.fromTraining(training, TrainerWorkloadActionType.DELETE))
        .thenReturn(workloadRequest);

    trainingService.deleteTraining(10L);

    assertAll(
        () -> verify(trainingRepository).findById(10L),
        () -> verify(trainerWorkloadRequestFactory).fromTraining(training, TrainerWorkloadActionType.DELETE),
        () -> verify(trainingRepository).delete(training),
        () -> verify(trainerWorkloadOutboxService).savePendingEvent(10L, workloadRequest));
  }

  @Test
  void deleteTrainingShouldThrowEntityNotFoundExceptionWhenTrainingDoesNotExist() {
    when(trainingRepository.findById(10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainingService.deleteTraining(10L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Training not found");

    verify(trainingRepository).findById(10L);
  }

  @Test
  void deleteTrainingShouldHaveBeanValidationViolationWhenTrainingIdIsNull() throws Exception {
    TrainingService target = trainingService;
    Method method = TrainingService.class.getMethod("deleteTraining", Long.class);

    Set<ConstraintViolation<TrainingService>> violations =
        validator.forExecutables().validateParameters(target, method, new Object[] {null});

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactly("Training id must not be null");
  }

  @Test
  void addTrainingShouldHaveBeanValidationViolationWhenRequestIsNull() throws Exception {
    TrainingService target = trainingService;
    Method method = TrainingService.class.getMethod("addTraining", AddTrainingRequest.class);

    Set<ConstraintViolation<TrainingService>> violations =
        validator.forExecutables().validateParameters(target, method, new Object[] {null});

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactly("Training request must not be null");
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

    assertAddTrainingRequestViolation(request, "Trainee username must not be blank");
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

    assertAddTrainingRequestViolation(request, "Trainer username must not be blank");
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

    assertAddTrainingRequestViolation(request, "Trainer username must not be blank");
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

    assertAddTrainingRequestViolation(request, "Training name must not be blank");
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

    assertAddTrainingRequestViolation(request, "Training type must not be blank");
  }

  @Test
  void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingDateIsNull() {
    AddTrainingRequest request =
        addTrainingRequest(
            "Training.Trainee", "password", "Training.Trainer", "Yoga Basics", "Yoga", null, 60);

    assertAddTrainingRequestViolation(request, "Training date must not be null");
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

    assertAddTrainingRequestViolation(request, "Training duration must not be null");
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

    assertAddTrainingRequestViolation(request, "Training duration must be positive");
  }

  @Test
  void getTraineeTrainingsShouldAuthenticateTraineeAndReturnMappedTrainings() {
    TraineeTrainingsRequest request =
        new TraineeTrainingsRequest(
            "Training.Trainee",
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
    when(trainingRepository.findByTraineeUsernameAndCriteria(
            "Training.Trainee", criteria, PageRequest.firstPage()))
        .thenReturn(List.of(training));
    when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(response);

    List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> verify(trainingMapper).toCriteria(request),
        () ->
            verify(trainingRepository)
                .findByTraineeUsernameAndCriteria(
                    "Training.Trainee", criteria, PageRequest.firstPage()),
        () -> verify(trainingMapper).toTraineeTrainingResponse(training));
  }

  @Test
  void getTraineeTrainingsShouldUseEmptyCriteriaWhenMapperReturnsNull() {
    TraineeTrainingsRequest request =
        new TraineeTrainingsRequest("Training.Trainee", null, null, null, null, null);
    Training training = validTraining();
    TraineeTrainingResponse response = traineeTrainingResponse();
    when(trainingMapper.toCriteria(request)).thenReturn(null);
    when(trainingRepository.findByTraineeUsernameAndCriteria(
            "Training.Trainee", TraineeTrainingCriteria.empty(), PageRequest.firstPage()))
        .thenReturn(List.of(training));
    when(trainingMapper.toTraineeTrainingResponse(training)).thenReturn(response);

    List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () ->
            verify(trainingRepository)
                .findByTraineeUsernameAndCriteria(
                    "Training.Trainee", TraineeTrainingCriteria.empty(), PageRequest.firstPage()));
  }

  @Test
  void getTrainerTrainingsShouldAuthenticateTrainerAndReturnMappedTrainings() {
    TrainerTrainingsRequest request =
        new TrainerTrainingsRequest(
            "Training.Trainer",
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28),
            "Trainee",
            PageRequest.firstPage());
    TrainerTrainingCriteria criteria =
        new TrainerTrainingCriteria(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), "Trainee");
    Training training = validTraining();
    TrainerTrainingResponse response = trainerTrainingResponse();
    when(trainingMapper.toCriteria(request)).thenReturn(criteria);
    when(trainingRepository.findByTrainerUsernameAndCriteria(
            "Training.Trainer", criteria, PageRequest.firstPage()))
        .thenReturn(List.of(training));
    when(trainingMapper.toTrainerTrainingResponse(training)).thenReturn(response);

    List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> verify(trainingMapper).toCriteria(request),
        () ->
            verify(trainingRepository)
                .findByTrainerUsernameAndCriteria(
                    "Training.Trainer", criteria, PageRequest.firstPage()),
        () -> verify(trainingMapper).toTrainerTrainingResponse(training));
  }

  @Test
  void getTrainerTrainingsShouldUseEmptyCriteriaWhenMapperReturnsNull() {
    TrainerTrainingsRequest request =
        new TrainerTrainingsRequest("Training.Trainer", null, null, null, null);
    Training training = validTraining();
    TrainerTrainingResponse response = trainerTrainingResponse();
    when(trainingMapper.toCriteria(request)).thenReturn(null);
    when(trainingRepository.findByTrainerUsernameAndCriteria(
            "Training.Trainer", TrainerTrainingCriteria.empty(), PageRequest.firstPage()))
        .thenReturn(List.of(training));
    when(trainingMapper.toTrainerTrainingResponse(training)).thenReturn(response);

    List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () ->
            verify(trainingRepository)
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
        trainerUsername,
        trainingName,
        trainingTypeName,
        trainingDate,
        trainingDuration);
  }

  private void assertAddTrainingRequestViolation(AddTrainingRequest request, String message) {
    assertThat(validator.validate(request)).extracting(ConstraintViolation::getMessage).contains(message);
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

  private static TrainerWorkloadRequest trainerWorkloadRequest(
      TrainerWorkloadActionType actionType) {
    return new TrainerWorkloadRequest(
        10L,
        "Training.Trainer",
        "Training",
        "Trainer",
        true,
        LocalDate.of(2026, 5, 3),
        60,
        actionType);
  }
}
