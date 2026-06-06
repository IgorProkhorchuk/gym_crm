package com.epam.gymcrm.service;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
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
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.UserRepository;
import com.epam.gymcrm.service.impl.TraineeServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @InjectMocks private TraineeServiceImpl traineeService;

  @Mock private TraineeRepository traineeRepository;

  @Mock private TrainerRepository trainerRepository;

  @Mock private UserRepository userRepository;

  @Mock private AuthenticationService authenticationService;

  @Mock private PasswordGenerator passwordGenerator;

  @Mock private UsernameGenerator usernameGenerator;

  @Mock private TraineeMapper traineeMapper;

  @Mock private TrainerMapper trainerMapper;

  @Test
  void createShouldGenerateUsernameAndPassword() {
    CreateTraineeRequest request = createTraineeRequest("John", "Doe", true);
    Trainee trainee = Trainee.builder().user(user("John", "Doe", null)).build();

    when(traineeMapper.toEntity(request)).thenReturn(trainee);
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(userRepository.findUsernamesByPattern("John.Doe%")).thenReturn(Collections.emptySet());
    when(usernameGenerator.generate("John", "Doe", Collections.emptySet())).thenReturn("John.Doe");

    UsernamePasswordResponse result = traineeService.create(request);

    assertAll(
        () -> assertThat(result.username()).isEqualTo("John.Doe"),
        () -> assertThat(result.password()).isEqualTo("Passw0rd12"),
        () -> assertThat(trainee.getUser().getUsername()).isEqualTo("John.Doe"),
        () -> assertThat(trainee.getUser().getPassword()).isEqualTo("Passw0rd12"),
        () -> verify(traineeMapper).toEntity(request),
        () -> verify(usernameGenerator).generate("John", "Doe", Collections.emptySet()),
        () -> verify(passwordGenerator).generate(),
        () -> verify(traineeRepository).save(trainee));
  }

  @Test
  void createShouldPassExistingUsernamesToGenerator() {
    CreateTraineeRequest request = createTraineeRequest("John", "Doe", true);
    Trainee newTrainee = Trainee.builder().user(user("John", "Doe", null)).build();

    Set<String> existingUsernames = Set.of("John.Doe", "John.Doe2", "John.Doering");
    when(traineeMapper.toEntity(request)).thenReturn(newTrainee);
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(userRepository.findUsernamesByPattern("John.Doe%")).thenReturn(existingUsernames);
    when(usernameGenerator.generate("John", "Doe", existingUsernames)).thenReturn("John.Doe1");

    traineeService.create(request);

    assertAll(
        () -> assertThat(newTrainee.getUser().getUsername()).isEqualTo("John.Doe1"),
        () -> verify(usernameGenerator).generate("John", "Doe", existingUsernames),
        () -> verify(traineeRepository).save(newTrainee));
  }

  @Test
  void createShouldThrowRuntimeExceptionWhenDaoFails() {
    CreateTraineeRequest request = createTraineeRequest("John", "Doe", true);
    Trainee trainee = Trainee.builder().user(user("John", "Doe", null)).build();
    RuntimeException exception = new RuntimeException("DAO failure");

    when(traineeMapper.toEntity(request)).thenReturn(trainee);
    when(userRepository.findUsernamesByPattern("John.Doe%")).thenReturn(Collections.emptySet());
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(usernameGenerator.generate("John", "Doe", Collections.emptySet())).thenReturn("John.Doe");
    doThrow(exception).when(traineeRepository).save(trainee);

    assertThatThrownBy(() -> traineeService.create(request)).isSameAs(exception);
  }

  @Test
  void createShouldHaveBeanValidationViolationWhenRequestIsNull() throws Exception {
    assertServiceParameterViolation(
        "create", CreateTraineeRequest.class, null, "Trainee request must not be null");
  }

  @Test
  void createShouldHaveBeanValidationViolationWhenFirstNameIsBlank() {
    assertRequestViolation(createTraineeRequest(" ", "Doe", true), "must not be blank");

    verifyNoInteractions(traineeMapper, usernameGenerator, passwordGenerator, traineeRepository);
  }

  @Test
  void createShouldHaveBeanValidationViolationWhenLastNameIsBlank() {
    assertRequestViolation(createTraineeRequest("John", " ", true), "must not be blank");

    verifyNoInteractions(traineeMapper, usernameGenerator, passwordGenerator, traineeRepository);
  }

  @Test
  void getProfileShouldReturnMappedAuthenticatedTrainee() {
    AuthRequest request = new AuthRequest("Jane.Doe", "password");
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    TraineeProfileResponse response = traineeProfileResponse(1L, "Jane.Doe", "Jane", "Doe");
    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);
    when(traineeMapper.toProfileResponse(trainee)).thenReturn(response);

    TraineeProfileResponse result = traineeService.getProfile(request);

    assertAll(
        () -> assertThat(result).isSameAs(response),
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(traineeMapper).toProfileResponse(trainee),
        () -> verifyNoMoreInteractions(authenticationService));
  }

  @Test
  void changePasswordShouldUpdateAuthenticatedTraineePassword() {
    ChangePasswordRequest request =
        new ChangePasswordRequest("Jane.Doe", "old-password", "new-password");
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    when(authenticationService.authenticateTrainee("Jane.Doe", "old-password")).thenReturn(trainee);

    traineeService.changePassword(request);

    assertAll(
        () -> assertThat(trainee.getUser().getPassword()).isEqualTo("new-password"),
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "old-password"),
        () -> verify(traineeRepository).save(trainee));
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
    ChangePasswordRequest request = new ChangePasswordRequest("Jane.Doe", "old-password", null);

    assertRequestViolation(request, "New password must not be blank");
  }

  @Test
  void changePasswordShouldHaveBeanValidationViolationWhenNewPasswordIsBlank() {
    ChangePasswordRequest request = new ChangePasswordRequest("Jane.Doe", "old-password", " ");

    assertRequestViolation(request, "New password must not be blank");
  }

  @Test
  void switchActiveStatusShouldSetAuthenticatedTraineeActiveWhenCurrentlyInactive() {
    AuthRequest request = new AuthRequest("Jane.Doe", "password");
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    trainee.getUser().setActive(false);
    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);

    traineeService.switchActiveStatus(request);

    assertAll(
        () -> assertThat(trainee.getUser().getActive()).isTrue(),
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(traineeRepository).save(trainee));
  }

  @Test
  void switchActiveStatusShouldSetAuthenticatedTraineeInactiveWhenCurrentlyActive() {
    AuthRequest request = new AuthRequest("Jane.Doe", "password");
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);

    traineeService.switchActiveStatus(request);

    assertAll(
        () -> assertThat(trainee.getUser().getActive()).isFalse(),
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(traineeRepository).save(trainee));
  }

  @Test
  void switchActiveStatusShouldFlipStatusOnEveryCall() {
    AuthRequest request = new AuthRequest("Jane.Doe", "password");
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);

    traineeService.switchActiveStatus(request);
    traineeService.switchActiveStatus(request);

    assertAll(
        () -> assertThat(trainee.getUser().getActive()).isTrue(),
        () ->
            verify(authenticationService, org.mockito.Mockito.times(2))
                .authenticateTrainee("Jane.Doe", "password"),
        () -> verify(traineeRepository, org.mockito.Mockito.times(2)).save(trainee));
  }

  @Test
  void deleteByUsernameShouldDeleteAuthenticatedTraineeById() {
    AuthRequest request = new AuthRequest("Jane.Doe", "password");
    Trainee trainee = trainee(15L, "Jane", "Doe", "Jane.Doe");
    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);

    traineeService.deleteByUsername(request);

    assertAll(
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(traineeRepository).delete(15L));
  }

  @Test
  void deleteByUsernameShouldNotDeleteWhenAuthenticationFails() {
    AuthRequest request = new AuthRequest("Deleted.Trainee", "password");
    AuthenticationException exception = new AuthenticationException("Invalid username or password");
    when(authenticationService.authenticateTrainee("Deleted.Trainee", "password"))
        .thenThrow(exception);

    assertThatThrownBy(() -> traineeService.deleteByUsername(request)).isSameAs(exception);

    assertAll(
        () -> verify(authenticationService).authenticateTrainee("Deleted.Trainee", "password"),
        () -> verifyNoInteractions(traineeRepository));
  }

  @Test
  void updateTrainersShouldReplaceAuthenticatedTraineeTrainers() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest(
            "Jane.Doe", "password", List.of("First.Trainer", "Second.Trainer"));
    Trainee trainee = trainee(17L, "Jane", "Doe", "Jane.Doe");
    Trainer oldTrainer = trainer(19L, "Old", "Trainer", "Old.Trainer");
    Trainer firstTrainer = trainer(20L, "First", "Trainer", "First.Trainer");
    Trainer secondTrainer = trainer(21L, "Second", "Trainer", "Second.Trainer");
    TrainerSummaryResponse firstResponse =
        trainerSummaryResponse(20L, "First.Trainer", "First", "Trainer");
    TrainerSummaryResponse secondResponse =
        trainerSummaryResponse(21L, "Second.Trainer", "Second", "Trainer");
    trainee.getTrainers().add(oldTrainer);

    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);
    when(trainerRepository.findByUsername("First.Trainer")).thenReturn(Optional.of(firstTrainer));
    when(trainerRepository.findByUsername("Second.Trainer")).thenReturn(Optional.of(secondTrainer));
    when(trainerMapper.toSummaryResponse(firstTrainer)).thenReturn(firstResponse);
    when(trainerMapper.toSummaryResponse(secondTrainer)).thenReturn(secondResponse);

    List<TrainerSummaryResponse> result = traineeService.updateTrainers(request);

    assertAll(
        () -> assertThat(result).containsExactly(firstResponse, secondResponse),
        () ->
            assertThat(trainee.getTrainers())
                .containsExactlyInAnyOrder(firstTrainer, secondTrainer),
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(trainerRepository).findByUsername("First.Trainer"),
        () -> verify(trainerRepository).findByUsername("Second.Trainer"),
        () -> verify(traineeRepository).save(trainee));
  }

  @Test
  void updateTrainersShouldIgnoreDuplicateTrainerUsernames() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest(
            "Jane.Doe", "password", List.of("First.Trainer", "First.Trainer"));
    Trainee trainee = trainee(17L, "Jane", "Doe", "Jane.Doe");
    Trainer trainer = trainer(20L, "First", "Trainer", "First.Trainer");
    TrainerSummaryResponse response =
        trainerSummaryResponse(20L, "First.Trainer", "First", "Trainer");

    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);
    when(trainerRepository.findByUsername("First.Trainer")).thenReturn(Optional.of(trainer));
    when(trainerMapper.toSummaryResponse(trainer)).thenReturn(response);

    List<TrainerSummaryResponse> result = traineeService.updateTrainers(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> assertThat(trainee.getTrainers()).containsExactly(trainer),
        () -> verify(trainerRepository).findByUsername("First.Trainer"),
        () -> verify(traineeRepository).save(trainee));
  }

  @Test
  void updateTrainersShouldClearTrainersWhenTrainerUsernamesAreEmpty() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest("Jane.Doe", "password", Collections.emptyList());
    Trainee trainee = trainee(17L, "Jane", "Doe", "Jane.Doe");
    trainee.getTrainers().add(trainer(19L, "Old", "Trainer", "Old.Trainer"));

    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);

    List<TrainerSummaryResponse> result = traineeService.updateTrainers(request);

    assertAll(
        () -> assertThat(result).isEmpty(),
        () -> assertThat(trainee.getTrainers()).isEmpty(),
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verifyNoInteractions(trainerRepository),
        () -> verify(traineeRepository).save(trainee));
  }

  @Test
  void updateTrainersShouldThrowEntityNotFoundExceptionWhenTrainerDoesNotExist() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest("Jane.Doe", "password", List.of("Unknown.Trainer"));
    Trainee trainee = trainee(17L, "Jane", "Doe", "Jane.Doe");
    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);
    when(trainerRepository.findByUsername("Unknown.Trainer")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> traineeService.updateTrainers(request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Trainer profile not found");

    assertAll(
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(trainerRepository).findByUsername("Unknown.Trainer"),
        () -> verifyNoMoreInteractions(traineeRepository));
  }

  @Test
  void updateTrainersShouldHaveBeanValidationViolationWhenTrainerUsernamesAreNull() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest("Jane.Doe", "password", null);

    assertRequestViolation(request, "Trainer usernames must not be null");

    assertAll(
        () -> verifyNoInteractions(authenticationService),
        () -> verifyNoInteractions(trainerRepository),
        () -> verifyNoInteractions(traineeRepository));
  }

  @Test
  void updateTrainersShouldHaveBeanValidationViolationWhenTrainerUsernameIsBlank() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest("Jane.Doe", "password", List.of(" "));

    assertRequestViolation(request, "Trainer username must not be blank");

    assertAll(
        () -> verifyNoInteractions(authenticationService),
        () -> verifyNoInteractions(trainerRepository),
        () -> verifyNoInteractions(traineeRepository));
  }

  @Test
  void updateShouldSaveAuthenticatedTraineeChanges() {
    UpdateTraineeRequest request = updateTraineeRequest(10L, "Jean", "Granger-Weasley");
    Trainee authenticatedTrainee = trainee(10L, "Hermione", "Granger", "Hermione.Granger");
    TraineeProfileResponse response =
        traineeProfileResponse(10L, "Hermione.Granger", "Jean", "Granger-Weasley");
    when(authenticationService.authenticateTrainee("Hermione.Granger", "password"))
        .thenReturn(authenticatedTrainee);
    when(traineeMapper.toProfileResponse(authenticatedTrainee)).thenReturn(response);

    TraineeProfileResponse result = traineeService.update(request);

    assertAll(
        () -> assertThat(result).isSameAs(response),
        () -> verify(authenticationService).authenticateTrainee("Hermione.Granger", "password"),
        () -> verify(traineeMapper).updateFromRequest(request, authenticatedTrainee),
        () -> verify(traineeRepository).save(authenticatedTrainee),
        () -> verify(traineeMapper).toProfileResponse(authenticatedTrainee),
        () -> verifyNoMoreInteractions(traineeRepository));
  }

  @Test
  void updateShouldThrowAuthenticationExceptionWhenTraineeDoesNotExist() {
    UpdateTraineeRequest request = updateTraineeRequest(10L, "Jean", "Granger");
    AuthenticationException exception = new AuthenticationException("Invalid username or password");
    when(authenticationService.authenticateTrainee("Hermione.Granger", "password"))
        .thenThrow(exception);

    assertThatThrownBy(() -> traineeService.update(request)).isSameAs(exception);

    verifyNoInteractions(traineeRepository);
  }

  @Test
  void updateShouldThrowRuntimeExceptionWhenDaoFails() {
    UpdateTraineeRequest request = updateTraineeRequest(10L, "Jean", "Granger");
    Trainee authenticatedTrainee = trainee(10L, "Hermione", "Granger", "Hermione.Granger");
    RuntimeException exception = new RuntimeException("DAO failure");
    when(authenticationService.authenticateTrainee("Hermione.Granger", "password"))
        .thenReturn(authenticatedTrainee);
    doThrow(exception).when(traineeRepository).save(authenticatedTrainee);

    assertThatThrownBy(() -> traineeService.update(request)).isSameAs(exception);
  }

  @Test
  void updateShouldHaveBeanValidationViolationWhenRequestIsNull() throws Exception {
    assertServiceParameterViolation(
        "update", UpdateTraineeRequest.class, null, "Update trainee request must not be null");
  }

  private static CreateTraineeRequest createTraineeRequest(
      String firstName, String lastName, Boolean active) {
    return new CreateTraineeRequest(
        firstName, lastName, LocalDate.of(1995, 1, 10), "Main Street, 123");
  }

  private static UpdateTraineeRequest updateTraineeRequest(
      Long id, String firstName, String lastName) {
    return new UpdateTraineeRequest(
        "Hermione.Granger",
        "password",
        firstName,
        lastName,
        LocalDate.of(1994, 9, 19),
        "Updated Address",
        true);
  }

  private static TraineeProfileResponse traineeProfileResponse(
      Long id, String username, String firstName, String lastName) {
    return new TraineeProfileResponse(
        username,
        firstName,
        lastName,
        true,
        LocalDate.of(1995, 1, 10),
        "Main Street, 123",
        List.of());
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
    Method method = TraineeService.class.getMethod(methodName, parameterType);
    TraineeService target = traineeService;

    assertThat(validator.forExecutables().validateParameters(target, method, new Object[] {argument}))
        .extracting(ConstraintViolation::getMessage)
        .contains(message);
  }
}
