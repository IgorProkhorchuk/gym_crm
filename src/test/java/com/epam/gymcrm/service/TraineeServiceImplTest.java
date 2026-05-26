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

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
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
import com.epam.gymcrm.service.impl.TraineeServiceImpl;
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

  @InjectMocks private TraineeServiceImpl traineeService;

  @Mock private TraineeDao traineeDao;

  @Mock private TrainerDao trainerDao;

  @Mock private com.epam.gymcrm.dao.UserDao userDao;

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
    when(userDao.findUsernamesByPattern("John.Doe%")).thenReturn(Collections.emptySet());
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
        () -> verify(traineeDao).save(trainee));
  }

  @Test
  void createShouldPassExistingUsernamesToGenerator() {
    CreateTraineeRequest request = createTraineeRequest("John", "Doe", true);
    Trainee newTrainee = Trainee.builder().user(user("John", "Doe", null)).build();

    Set<String> existingUsernames = Set.of("John.Doe", "John.Doe2", "John.Doering");
    when(traineeMapper.toEntity(request)).thenReturn(newTrainee);
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(userDao.findUsernamesByPattern("John.Doe%")).thenReturn(existingUsernames);
    when(usernameGenerator.generate("John", "Doe", existingUsernames)).thenReturn("John.Doe1");

    traineeService.create(request);

    assertAll(
        () -> assertThat(newTrainee.getUser().getUsername()).isEqualTo("John.Doe1"),
        () -> verify(usernameGenerator).generate("John", "Doe", existingUsernames),
        () -> verify(traineeDao).save(newTrainee));
  }

  @Test
  void createShouldThrowRuntimeExceptionWhenDaoFails() {
    CreateTraineeRequest request = createTraineeRequest("John", "Doe", true);
    Trainee trainee = Trainee.builder().user(user("John", "Doe", null)).build();
    RuntimeException exception = new RuntimeException("DAO failure");

    when(traineeMapper.toEntity(request)).thenReturn(trainee);
    when(userDao.findUsernamesByPattern("John.Doe%")).thenReturn(Collections.emptySet());
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(usernameGenerator.generate("John", "Doe", Collections.emptySet())).thenReturn("John.Doe");
    doThrow(exception).when(traineeDao).save(trainee);

    assertThatThrownBy(() -> traineeService.create(request)).isSameAs(exception);
  }

  @Test
  void createShouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
    assertThatThrownBy(() -> traineeService.create(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainee request must not be null");
  }

  @Test
  void createShouldThrowIllegalArgumentExceptionWhenFirstNameIsBlank() {
    assertThatThrownBy(() -> traineeService.create(createTraineeRequest(" ", "Doe", true)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("First name must not be blank");

    verifyNoInteractions(traineeMapper, usernameGenerator, passwordGenerator, traineeDao);
  }

  @Test
  void createShouldThrowIllegalArgumentExceptionWhenLastNameIsBlank() {
    assertThatThrownBy(() -> traineeService.create(createTraineeRequest("John", " ", true)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Last name must not be blank");

    verifyNoInteractions(traineeMapper, usernameGenerator, passwordGenerator, traineeDao);
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
        () -> verify(traineeDao).save(trainee));
  }

  @Test
  void changePasswordShouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
    assertThatThrownBy(() -> traineeService.changePassword(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Change password request must not be null");
  }

  @Test
  void changePasswordShouldThrowIllegalArgumentExceptionWhenNewPasswordIsNull() {
    ChangePasswordRequest request = new ChangePasswordRequest("Jane.Doe", "old-password", null);

    assertThatThrownBy(() -> traineeService.changePassword(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New password must not be blank");
  }

  @Test
  void changePasswordShouldThrowIllegalArgumentExceptionWhenNewPasswordIsBlank() {
    ChangePasswordRequest request = new ChangePasswordRequest("Jane.Doe", "old-password", " ");

    assertThatThrownBy(() -> traineeService.changePassword(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New password must not be blank");
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
        () -> verify(traineeDao).save(trainee));
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
        () -> verify(traineeDao).save(trainee));
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
        () -> verify(traineeDao, org.mockito.Mockito.times(2)).save(trainee));
  }

  @Test
  void deleteByUsernameShouldDeleteAuthenticatedTraineeById() {
    AuthRequest request = new AuthRequest("Jane.Doe", "password");
    Trainee trainee = trainee(15L, "Jane", "Doe", "Jane.Doe");
    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);

    traineeService.deleteByUsername(request);

    assertAll(
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(traineeDao).delete(15L));
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
        () -> verifyNoInteractions(traineeDao));
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
    when(trainerDao.findByUsername("First.Trainer")).thenReturn(Optional.of(firstTrainer));
    when(trainerDao.findByUsername("Second.Trainer")).thenReturn(Optional.of(secondTrainer));
    when(trainerMapper.toSummaryResponse(firstTrainer)).thenReturn(firstResponse);
    when(trainerMapper.toSummaryResponse(secondTrainer)).thenReturn(secondResponse);

    List<TrainerSummaryResponse> result = traineeService.updateTrainers(request);

    assertAll(
        () -> assertThat(result).containsExactly(firstResponse, secondResponse),
        () ->
            assertThat(trainee.getTrainers())
                .containsExactlyInAnyOrder(firstTrainer, secondTrainer),
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(trainerDao).findByUsername("First.Trainer"),
        () -> verify(trainerDao).findByUsername("Second.Trainer"),
        () -> verify(traineeDao).save(trainee));
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
    when(trainerDao.findByUsername("First.Trainer")).thenReturn(Optional.of(trainer));
    when(trainerMapper.toSummaryResponse(trainer)).thenReturn(response);

    List<TrainerSummaryResponse> result = traineeService.updateTrainers(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> assertThat(trainee.getTrainers()).containsExactly(trainer),
        () -> verify(trainerDao).findByUsername("First.Trainer"),
        () -> verify(traineeDao).save(trainee));
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
        () -> verifyNoInteractions(trainerDao),
        () -> verify(traineeDao).save(trainee));
  }

  @Test
  void updateTrainersShouldThrowEntityNotFoundExceptionWhenTrainerDoesNotExist() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest("Jane.Doe", "password", List.of("Unknown.Trainer"));
    Trainee trainee = trainee(17L, "Jane", "Doe", "Jane.Doe");
    when(authenticationService.authenticateTrainee("Jane.Doe", "password")).thenReturn(trainee);
    when(trainerDao.findByUsername("Unknown.Trainer")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> traineeService.updateTrainers(request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Trainer profile not found");

    assertAll(
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(trainerDao).findByUsername("Unknown.Trainer"),
        () -> verifyNoMoreInteractions(traineeDao));
  }

  @Test
  void updateTrainersShouldThrowIllegalArgumentExceptionWhenTrainerUsernamesAreNull() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest("Jane.Doe", "password", null);

    assertThatThrownBy(() -> traineeService.updateTrainers(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainer usernames must not be null");

    assertAll(
        () -> verifyNoInteractions(authenticationService),
        () -> verifyNoInteractions(trainerDao),
        () -> verifyNoInteractions(traineeDao));
  }

  @Test
  void updateTrainersShouldThrowIllegalArgumentExceptionWhenTrainerUsernameIsBlank() {
    UpdateTraineeTrainersRequest request =
        new UpdateTraineeTrainersRequest("Jane.Doe", "password", List.of(" "));

    assertThatThrownBy(() -> traineeService.updateTrainers(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainer username must not be blank");

    assertAll(
        () -> verifyNoInteractions(authenticationService),
        () -> verifyNoInteractions(trainerDao),
        () -> verifyNoInteractions(traineeDao));
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
        () -> verify(traineeDao).save(authenticatedTrainee),
        () -> verify(traineeMapper).toProfileResponse(authenticatedTrainee),
        () -> verifyNoMoreInteractions(traineeDao));
  }

  @Test
  void updateShouldThrowAuthenticationExceptionWhenTraineeDoesNotExist() {
    UpdateTraineeRequest request = updateTraineeRequest(10L, "Jean", "Granger");
    AuthenticationException exception = new AuthenticationException("Invalid username or password");
    when(authenticationService.authenticateTrainee("Hermione.Granger", "password"))
        .thenThrow(exception);

    assertThatThrownBy(() -> traineeService.update(request)).isSameAs(exception);

    verifyNoInteractions(traineeDao);
  }

  @Test
  void updateShouldThrowRuntimeExceptionWhenDaoFails() {
    UpdateTraineeRequest request = updateTraineeRequest(10L, "Jean", "Granger");
    Trainee authenticatedTrainee = trainee(10L, "Hermione", "Granger", "Hermione.Granger");
    RuntimeException exception = new RuntimeException("DAO failure");
    when(authenticationService.authenticateTrainee("Hermione.Granger", "password"))
        .thenReturn(authenticatedTrainee);
    doThrow(exception).when(traineeDao).save(authenticatedTrainee);

    assertThatThrownBy(() -> traineeService.update(request)).isSameAs(exception);
  }

  @Test
  void updateShouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
    assertThatThrownBy(() -> traineeService.update(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Update trainee request must not be null");
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
}
