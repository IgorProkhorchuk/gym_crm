package com.epam.gymcrm.service;

import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static com.epam.gymcrm.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import com.epam.gymcrm.repository.UserRepository;
import com.epam.gymcrm.service.impl.TrainerServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @InjectMocks private TrainerServiceImpl trainerService;

  @Mock private TrainerRepository trainerRepository;

  @Mock private TraineeRepository traineeRepository;

  @Mock private UserRepository userRepository;

  @Mock private TrainingTypeRepository trainingTypeRepository;

  @Mock private AuthenticationService authenticationService;

  @Mock private PasswordGenerator passwordGenerator;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private UsernameGenerator usernameGenerator;

  @Mock private TrainerMapper trainerMapper;

  @Test
  void createShouldGenerateUsernameAndPassword() {
    CreateTrainerRequest request = createTrainerRequest("Severus", "Snape", "Fitness", true);
    Trainer trainer =
        Trainer.builder()
            .user(user("Severus", "Snape", null))
            .specialization(trainingType("Fitness"))
            .build();
    TrainingType specialization = trainingType("Fitness");

    when(trainerMapper.toEntity(request)).thenReturn(trainer);
    when(trainingTypeRepository.findByName("Fitness")).thenReturn(Optional.of(specialization));
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(passwordEncoder.encode("Passw0rd12")).thenReturn("encoded-Passw0rd12");
    when(userRepository.findUsernamesByPattern("Severus.Snape%")).thenReturn(Collections.emptySet());
    when(usernameGenerator.generate("Severus", "Snape", Collections.emptySet()))
        .thenReturn("Severus.Snape");

    UsernamePasswordResponse result = trainerService.create(request);

    assertAll(
        () -> assertThat(result.username()).isEqualTo("Severus.Snape"),
        () -> assertThat(result.password()).isEqualTo("Passw0rd12"),
        () -> assertThat(trainer.getUser().getUsername()).isEqualTo("Severus.Snape"),
        () -> assertThat(trainer.getUser().getPassword()).isEqualTo("encoded-Passw0rd12"),
        () -> assertThat(trainer.getSpecialization()).isSameAs(specialization),
        () -> verify(trainerMapper).toEntity(request),
        () -> verify(usernameGenerator).generate("Severus", "Snape", Collections.emptySet()),
        () -> verify(passwordGenerator).generate(),
        () -> verify(passwordEncoder).encode("Passw0rd12"),
        () -> verify(trainerRepository).save(trainer));
  }

  @Test
  void createShouldPassExistingUsernamesToGenerator() {
    CreateTrainerRequest request = createTrainerRequest("Severus", "Snape", "Fitness", true);
    Trainer newTrainer =
        Trainer.builder()
            .user(user("Severus", "Snape", null))
            .specialization(trainingType("Fitness"))
            .build();

    Set<String> existingUsernames = Set.of("Severus.Snape", "Severus.Snape2", "Severus.Snapely");
    when(trainerMapper.toEntity(request)).thenReturn(newTrainer);
    when(trainingTypeRepository.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(passwordEncoder.encode("Passw0rd12")).thenReturn("encoded-Passw0rd12");
    when(userRepository.findUsernamesByPattern("Severus.Snape%")).thenReturn(existingUsernames);
    when(usernameGenerator.generate("Severus", "Snape", existingUsernames))
        .thenReturn("Severus.Snape1");

    trainerService.create(request);

    assertAll(
        () -> assertThat(newTrainer.getUser().getUsername()).isEqualTo("Severus.Snape1"),
        () -> verify(usernameGenerator).generate("Severus", "Snape", existingUsernames),
        () -> verify(trainerRepository).save(newTrainer));
  }

  @Test
  void createShouldThrowRuntimeExceptionWhenDaoFails() {
    CreateTrainerRequest request = createTrainerRequest("Severus", "Snape", "Fitness", true);
    Trainer trainer =
        Trainer.builder()
            .user(user("Severus", "Snape", null))
            .specialization(trainingType("Fitness"))
            .build();
    RuntimeException exception = new RuntimeException("DAO failure");

    when(trainerMapper.toEntity(request)).thenReturn(trainer);
    when(trainingTypeRepository.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
    when(userRepository.findUsernamesByPattern("Severus.Snape%")).thenReturn(Collections.emptySet());
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(passwordEncoder.encode("Passw0rd12")).thenReturn("encoded-Passw0rd12");
    when(usernameGenerator.generate("Severus", "Snape", Collections.emptySet()))
        .thenReturn("Severus.Snape");
    doThrow(exception).when(trainerRepository).save(trainer);

    assertThatThrownBy(() -> trainerService.create(request)).isSameAs(exception);
  }

  @Test
  void createShouldHaveBeanValidationViolationWhenRequestIsNull() throws Exception {
    assertServiceParameterViolation(
        "create", CreateTrainerRequest.class, null, "Trainer request must not be null");
  }

  @Test
  void createShouldHaveBeanValidationViolationWhenFirstNameIsBlank() {
    CreateTrainerRequest request = createTrainerRequest(" ", "Snape", "Fitness", true);

    assertRequestViolation(request, "must not be blank");

    verifyNoInteractions(
        trainingTypeRepository, trainerMapper, usernameGenerator, passwordGenerator, trainerRepository);
  }

  @Test
  void createShouldHaveBeanValidationViolationWhenLastNameIsBlank() {
    CreateTrainerRequest request = createTrainerRequest("Severus", " ", "Fitness", true);

    assertRequestViolation(request, "must not be blank");

    verifyNoInteractions(
        trainingTypeRepository, trainerMapper, usernameGenerator, passwordGenerator, trainerRepository);
  }

  @Test
  void createShouldHaveBeanValidationViolationWhenSpecializationIsNull() {
    CreateTrainerRequest request = createTrainerRequest("Severus", "Snape", null, true);

    assertRequestViolation(request, "must not be blank");
  }

  @Test
  void createShouldHaveBeanValidationViolationWhenSpecializationIsBlank() {
    CreateTrainerRequest request = createTrainerRequest("Severus", "Snape", " ", true);

    assertRequestViolation(request, "must not be blank");
  }

  @Test
  void createShouldThrowEntityNotFoundExceptionWhenSpecializationDoesNotExist() {
    CreateTrainerRequest request = createTrainerRequest("Severus", "Snape", "Unknown", true);
    when(trainingTypeRepository.findByName("Unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainerService.create(request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Training type not found");
  }

  @Test
  void getProfileShouldReturnMappedAuthenticatedTrainer() {
    AuthRequest request = new AuthRequest("John.Coach");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    TrainerProfileResponse response =
        trainerProfileResponse(1L, "John.Coach", "John", "Coach", "Fitness");
    when(trainerRepository.findByUsername("John.Coach")).thenReturn(Optional.of(trainer));
    when(trainerMapper.toProfileResponse(trainer)).thenReturn(response);

    TrainerProfileResponse result = trainerService.getProfile(request);

    assertAll(
        () -> assertThat(result).isSameAs(response),
        () -> verify(trainerRepository).findByUsername("John.Coach"),
        () -> verify(trainerMapper).toProfileResponse(trainer),
        () -> verifyNoMoreInteractions(trainerRepository));
  }

  @Test
  void changePasswordShouldUpdateAuthenticatedTrainerPassword() {
    ChangePasswordRequest request =
        new ChangePasswordRequest("John.Coach", "old-password", "new-password");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(authenticationService.authenticateTrainer("John.Coach", "old-password"))
        .thenReturn(trainer);
    when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

    trainerService.changePassword(request);

    assertAll(
        () -> assertThat(trainer.getUser().getPassword()).isEqualTo("encoded-new-password"),
        () -> verify(authenticationService).authenticateTrainer("John.Coach", "old-password"),
        () -> verify(passwordEncoder).encode("new-password"),
        () -> verify(trainerRepository).save(trainer));
  }

  @Test
  void changePasswordShouldHaveBeanValidationViolationWhenRequestIsNull() throws Exception {
    assertServiceParameterViolation(
        "changePassword",
        ChangePasswordRequest.class,
        null,
        "Change password request must not be null");
  }

  @Test
  void changePasswordShouldHaveBeanValidationViolationWhenNewPasswordIsNull() {
    ChangePasswordRequest request = new ChangePasswordRequest("John.Coach", "old-password", null);

    assertRequestViolation(request, "New password must not be blank");
  }

  @Test
  void changePasswordShouldHaveBeanValidationViolationWhenNewPasswordIsBlank() {
    ChangePasswordRequest request = new ChangePasswordRequest("John.Coach", "old-password", " ");

    assertRequestViolation(request, "New password must not be blank");
  }

  @Test
  void switchActiveStatusShouldSetAuthenticatedTrainerActiveWhenCurrentlyInactive() {
    AuthRequest request = new AuthRequest("John.Coach");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    trainer.getUser().setActive(false);
    when(trainerRepository.findByUsername("John.Coach")).thenReturn(Optional.of(trainer));

    trainerService.switchActiveStatus(request);

    assertAll(
        () -> assertThat(trainer.getUser().getActive()).isTrue(),
        () -> verify(trainerRepository).findByUsername("John.Coach"),
        () -> verify(trainerRepository).save(trainer));
  }

  @Test
  void switchActiveStatusShouldSetAuthenticatedTrainerInactiveWhenCurrentlyActive() {
    AuthRequest request = new AuthRequest("John.Coach");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(trainerRepository.findByUsername("John.Coach")).thenReturn(Optional.of(trainer));

    trainerService.switchActiveStatus(request);

    assertAll(
        () -> assertThat(trainer.getUser().getActive()).isFalse(),
        () -> verify(trainerRepository).findByUsername("John.Coach"),
        () -> verify(trainerRepository).save(trainer));
  }

  @Test
  void switchActiveStatusShouldFlipStatusOnEveryCall() {
    AuthRequest request = new AuthRequest("John.Coach");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(trainerRepository.findByUsername("John.Coach")).thenReturn(Optional.of(trainer));

    trainerService.switchActiveStatus(request);
    trainerService.switchActiveStatus(request);

    assertAll(
        () -> assertThat(trainer.getUser().getActive()).isTrue(),
        () ->
            verify(trainerRepository, org.mockito.Mockito.times(2)).findByUsername("John.Coach"),
        () -> verify(trainerRepository, org.mockito.Mockito.times(2)).save(trainer));
  }

  @Test
  void getUnassignedTrainersShouldAuthenticateTraineeAndReturnMappedDaoResult() {
    AuthRequest request = new AuthRequest("Jane.Doe");
    Trainer trainer = trainer("Available", "Trainer", "Available.Trainer");
    TrainerSummaryResponse response =
        trainerSummaryResponse(1L, "Available.Trainer", "Available", "Trainer");
    when(traineeRepository.findByUsername("Jane.Doe"))
        .thenReturn(Optional.of(com.epam.gymcrm.TestFixtures.trainee("Jane", "Doe", "Jane.Doe")));
    when(trainerRepository.findNotAssignedToTrainee("Jane.Doe")).thenReturn(List.of(trainer));
    when(trainerMapper.toSummaryResponse(trainer)).thenReturn(response);

    List<TrainerSummaryResponse> result = trainerService.getUnassignedTrainers(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> verify(traineeRepository).findByUsername("Jane.Doe"),
        () -> verify(trainerRepository).findNotAssignedToTrainee("Jane.Doe"),
        () -> verify(trainerMapper).toSummaryResponse(trainer));
  }

  @Test
  void updateShouldSaveAuthenticatedTrainerChanges() {
    UpdateTrainerRequest request = updateTrainerRequest(22L, "Minnie", "McGonagall", "Yoga");
    Trainer authenticatedTrainer = trainer(22L, "Minerva", "McGonagall", "Minerva.McGonagall");
    TrainingType specialization = trainingType("Yoga");
    TrainerProfileResponse response =
        trainerProfileResponse(22L, "Minerva.McGonagall", "Minnie", "McGonagall", "Yoga");
    when(trainerRepository.findByUsername("Minerva.McGonagall")).thenReturn(Optional.of(authenticatedTrainer));
    when(trainingTypeRepository.findByName("Yoga")).thenReturn(Optional.of(specialization));
    when(trainerMapper.toProfileResponse(authenticatedTrainer)).thenReturn(response);

    TrainerProfileResponse result = trainerService.update(request);

    assertAll(
        () -> assertThat(result).isSameAs(response),
        () -> assertThat(authenticatedTrainer.getSpecialization()).isSameAs(specialization),
        () -> verify(trainerRepository).findByUsername("Minerva.McGonagall"),
        () -> verify(trainerMapper).updateFromRequest(request, authenticatedTrainer),
        () -> verify(trainingTypeRepository).findByName("Yoga"),
        () -> verify(trainerRepository).save(authenticatedTrainer),
        () -> verify(trainerMapper).toProfileResponse(authenticatedTrainer),
        () -> verifyNoMoreInteractions(trainerRepository));
  }

  @Test
  void updateShouldThrowAuthenticationExceptionWhenTrainerDoesNotExist() {
    UpdateTrainerRequest request = updateTrainerRequest(22L, "Minnie", "McGonagall", "Yoga");
    when(trainerRepository.findByUsername("Minerva.McGonagall")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainerService.update(request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Trainer profile not found");

    assertAll(
        () -> verifyNoInteractions(trainingTypeRepository),
        () -> verify(trainerRepository).findByUsername("Minerva.McGonagall"),
        () -> verifyNoMoreInteractions(trainerRepository));
  }

  @Test
  void updateShouldThrowRuntimeExceptionWhenDaoFails() {
    UpdateTrainerRequest request = updateTrainerRequest(22L, "Minnie", "McGonagall", "Fitness");
    Trainer authenticatedTrainer = trainer(22L, "Minerva", "McGonagall", "Minerva.McGonagall");
    RuntimeException exception = new RuntimeException("DAO failure");
    when(trainerRepository.findByUsername("Minerva.McGonagall")).thenReturn(Optional.of(authenticatedTrainer));
    when(trainingTypeRepository.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
    doThrow(exception).when(trainerRepository).save(authenticatedTrainer);

    assertThatThrownBy(() -> trainerService.update(request)).isSameAs(exception);
  }

  @Test
  void updateShouldHaveBeanValidationViolationWhenRequestIsNull() throws Exception {
    assertServiceParameterViolation(
        "update", UpdateTrainerRequest.class, null, "Update trainer request must not be null");
  }

  private static CreateTrainerRequest createTrainerRequest(
      String firstName, String lastName, String specialization, Boolean active) {
    return new CreateTrainerRequest(firstName, lastName, specialization);
  }

  private static UpdateTrainerRequest updateTrainerRequest(
      Long id, String firstName, String lastName, String specialization) {
    return new UpdateTrainerRequest(
        "Minerva.McGonagall", firstName, lastName, specialization, true);
  }

  private static TrainerProfileResponse trainerProfileResponse(
      Long id, String username, String firstName, String lastName, String specialization) {
    return new TrainerProfileResponse(username, firstName, lastName, true, specialization);
  }

  private static TrainerSummaryResponse trainerSummaryResponse(
      Long id, String username, String firstName, String lastName) {
    return new TrainerSummaryResponse(username, firstName, lastName, "Fitness");
  }

  private void assertRequestViolation(Object request, String message) {
    assertThat(validator.validate(request)).extracting(ConstraintViolation::getMessage).contains(message);
  }

  private void assertServiceParameterViolation(
      String methodName, Class<?> parameterType, Object argument, String message) throws Exception {
    Method method = TrainerService.class.getMethod(methodName, parameterType);
    TrainerService target = trainerService;

    assertThat(validator.forExecutables().validateParameters(target, method, new Object[] {argument}))
        .extracting(ConstraintViolation::getMessage)
        .contains(message);
  }
}
