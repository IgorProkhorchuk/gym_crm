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

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TrainerMapper;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.service.impl.TrainerServiceImpl;
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
class TrainerServiceImplTest {

  @InjectMocks private TrainerServiceImpl trainerService;

  @Mock private TrainerDao trainerDao;

  @Mock private com.epam.gymcrm.dao.UserDao userDao;

  @Mock private TrainingTypeDao trainingTypeDao;

  @Mock private AuthenticationService authenticationService;

  @Mock private PasswordGenerator passwordGenerator;

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
    when(trainingTypeDao.findByName("Fitness")).thenReturn(Optional.of(specialization));
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(userDao.findUsernamesByPattern("Severus.Snape%")).thenReturn(Collections.emptySet());
    when(usernameGenerator.generate("Severus", "Snape", Collections.emptySet()))
        .thenReturn("Severus.Snape");

    UsernamePasswordResponse result = trainerService.create(request);

    assertAll(
        () -> assertThat(result.username()).isEqualTo("Severus.Snape"),
        () -> assertThat(result.password()).isEqualTo("Passw0rd12"),
        () -> assertThat(trainer.getUser().getUsername()).isEqualTo("Severus.Snape"),
        () -> assertThat(trainer.getUser().getPassword()).isEqualTo("Passw0rd12"),
        () -> assertThat(trainer.getSpecialization()).isSameAs(specialization),
        () -> verify(trainerMapper).toEntity(request),
        () -> verify(usernameGenerator).generate("Severus", "Snape", Collections.emptySet()),
        () -> verify(passwordGenerator).generate(),
        () -> verify(trainerDao).save(trainer));
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
    when(trainingTypeDao.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(userDao.findUsernamesByPattern("Severus.Snape%")).thenReturn(existingUsernames);
    when(usernameGenerator.generate("Severus", "Snape", existingUsernames))
        .thenReturn("Severus.Snape1");

    trainerService.create(request);

    assertAll(
        () -> assertThat(newTrainer.getUser().getUsername()).isEqualTo("Severus.Snape1"),
        () -> verify(usernameGenerator).generate("Severus", "Snape", existingUsernames),
        () -> verify(trainerDao).save(newTrainer));
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
    when(trainingTypeDao.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
    when(userDao.findUsernamesByPattern("Severus.Snape%")).thenReturn(Collections.emptySet());
    when(passwordGenerator.generate()).thenReturn("Passw0rd12");
    when(usernameGenerator.generate("Severus", "Snape", Collections.emptySet()))
        .thenReturn("Severus.Snape");
    doThrow(exception).when(trainerDao).save(trainer);

    assertThatThrownBy(() -> trainerService.create(request)).isSameAs(exception);
  }

  @Test
  void createShouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
    assertThatThrownBy(() -> trainerService.create(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainer request must not be null");
  }

  @Test
  void createShouldThrowIllegalArgumentExceptionWhenFirstNameIsBlank() {
    CreateTrainerRequest request = createTrainerRequest(" ", "Snape", "Fitness", true);

    assertThatThrownBy(() -> trainerService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("First name must not be blank");

    verifyNoInteractions(
        trainingTypeDao, trainerMapper, usernameGenerator, passwordGenerator, trainerDao);
  }

  @Test
  void createShouldThrowIllegalArgumentExceptionWhenLastNameIsBlank() {
    CreateTrainerRequest request = createTrainerRequest("Severus", " ", "Fitness", true);

    assertThatThrownBy(() -> trainerService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Last name must not be blank");

    verifyNoInteractions(
        trainingTypeDao, trainerMapper, usernameGenerator, passwordGenerator, trainerDao);
  }

  @Test
  void createShouldThrowIllegalArgumentExceptionWhenSpecializationIsNull() {
    CreateTrainerRequest request = createTrainerRequest("Severus", "Snape", null, true);

    assertThatThrownBy(() -> trainerService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainer specialization must not be null");
  }

  @Test
  void createShouldThrowIllegalArgumentExceptionWhenSpecializationIsBlank() {
    CreateTrainerRequest request = createTrainerRequest("Severus", "Snape", " ", true);

    assertThatThrownBy(() -> trainerService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Trainer specialization must not be blank");
  }

  @Test
  void createShouldThrowEntityNotFoundExceptionWhenSpecializationDoesNotExist() {
    CreateTrainerRequest request = createTrainerRequest("Severus", "Snape", "Unknown", true);
    when(trainingTypeDao.findByName("Unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainerService.create(request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Training type not found");
  }

  @Test
  void getProfileShouldReturnMappedAuthenticatedTrainer() {
    AuthRequest request = new AuthRequest("John.Coach", "password");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    TrainerProfileResponse response =
        trainerProfileResponse(1L, "John.Coach", "John", "Coach", "Fitness");
    when(authenticationService.authenticateTrainer("John.Coach", "password")).thenReturn(trainer);
    when(trainerMapper.toProfileResponse(trainer)).thenReturn(response);

    TrainerProfileResponse result = trainerService.getProfile(request);

    assertAll(
        () -> assertThat(result).isSameAs(response),
        () -> verify(authenticationService).authenticateTrainer("John.Coach", "password"),
        () -> verify(trainerMapper).toProfileResponse(trainer),
        () -> verifyNoMoreInteractions(authenticationService));
  }

  @Test
  void changePasswordShouldUpdateAuthenticatedTrainerPassword() {
    ChangePasswordRequest request =
        new ChangePasswordRequest("John.Coach", "old-password", "new-password");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(authenticationService.authenticateTrainer("John.Coach", "old-password"))
        .thenReturn(trainer);

    trainerService.changePassword(request);

    assertAll(
        () -> assertThat(trainer.getUser().getPassword()).isEqualTo("new-password"),
        () -> verify(authenticationService).authenticateTrainer("John.Coach", "old-password"),
        () -> verify(trainerDao).save(trainer));
  }

  @Test
  void changePasswordShouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
    assertThatThrownBy(() -> trainerService.changePassword(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Change password request must not be null");
  }

  @Test
  void changePasswordShouldThrowIllegalArgumentExceptionWhenNewPasswordIsNull() {
    ChangePasswordRequest request = new ChangePasswordRequest("John.Coach", "old-password", null);

    assertThatThrownBy(() -> trainerService.changePassword(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New password must not be blank");
  }

  @Test
  void changePasswordShouldThrowIllegalArgumentExceptionWhenNewPasswordIsBlank() {
    ChangePasswordRequest request = new ChangePasswordRequest("John.Coach", "old-password", " ");

    assertThatThrownBy(() -> trainerService.changePassword(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New password must not be blank");
  }

  @Test
  void switchActiveStatusShouldSetAuthenticatedTrainerActiveWhenCurrentlyInactive() {
    AuthRequest request = new AuthRequest("John.Coach", "password");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    trainer.getUser().setActive(false);
    when(authenticationService.authenticateTrainer("John.Coach", "password")).thenReturn(trainer);

    trainerService.switchActiveStatus(request);

    assertAll(
        () -> assertThat(trainer.getUser().getActive()).isTrue(),
        () -> verify(authenticationService).authenticateTrainer("John.Coach", "password"),
        () -> verify(trainerDao).save(trainer));
  }

  @Test
  void switchActiveStatusShouldSetAuthenticatedTrainerInactiveWhenCurrentlyActive() {
    AuthRequest request = new AuthRequest("John.Coach", "password");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(authenticationService.authenticateTrainer("John.Coach", "password")).thenReturn(trainer);

    trainerService.switchActiveStatus(request);

    assertAll(
        () -> assertThat(trainer.getUser().getActive()).isFalse(),
        () -> verify(authenticationService).authenticateTrainer("John.Coach", "password"),
        () -> verify(trainerDao).save(trainer));
  }

  @Test
  void switchActiveStatusShouldFlipStatusOnEveryCall() {
    AuthRequest request = new AuthRequest("John.Coach", "password");
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(authenticationService.authenticateTrainer("John.Coach", "password")).thenReturn(trainer);

    trainerService.switchActiveStatus(request);
    trainerService.switchActiveStatus(request);

    assertAll(
        () -> assertThat(trainer.getUser().getActive()).isTrue(),
        () ->
            verify(authenticationService, org.mockito.Mockito.times(2))
                .authenticateTrainer("John.Coach", "password"),
        () -> verify(trainerDao, org.mockito.Mockito.times(2)).save(trainer));
  }

  @Test
  void getUnassignedTrainersShouldAuthenticateTraineeAndReturnMappedDaoResult() {
    AuthRequest request = new AuthRequest("Jane.Doe", "password");
    Trainer trainer = trainer("Available", "Trainer", "Available.Trainer");
    TrainerSummaryResponse response =
        trainerSummaryResponse(1L, "Available.Trainer", "Available", "Trainer");
    when(authenticationService.authenticateTrainee("Jane.Doe", "password"))
        .thenReturn(com.epam.gymcrm.TestFixtures.trainee("Jane", "Doe", "Jane.Doe"));
    when(trainerDao.findNotAssignedToTrainee("Jane.Doe")).thenReturn(List.of(trainer));
    when(trainerMapper.toSummaryResponse(trainer)).thenReturn(response);

    List<TrainerSummaryResponse> result = trainerService.getUnassignedTrainers(request);

    assertAll(
        () -> assertThat(result).containsExactly(response),
        () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
        () -> verify(trainerDao).findNotAssignedToTrainee("Jane.Doe"),
        () -> verify(trainerMapper).toSummaryResponse(trainer));
  }

  @Test
  void updateShouldSaveAuthenticatedTrainerChanges() {
    UpdateTrainerRequest request = updateTrainerRequest(22L, "Minnie", "McGonagall", "Yoga");
    Trainer authenticatedTrainer = trainer(22L, "Minerva", "McGonagall", "Minerva.McGonagall");
    TrainingType specialization = trainingType("Yoga");
    TrainerProfileResponse response =
        trainerProfileResponse(22L, "Minerva.McGonagall", "Minnie", "McGonagall", "Yoga");
    when(authenticationService.authenticateTrainer("Minerva.McGonagall", "password"))
        .thenReturn(authenticatedTrainer);
    when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(specialization));
    when(trainerMapper.toProfileResponse(authenticatedTrainer)).thenReturn(response);

    TrainerProfileResponse result = trainerService.update(request);

    assertAll(
        () -> assertThat(result).isSameAs(response),
        () -> assertThat(authenticatedTrainer.getSpecialization()).isSameAs(specialization),
        () -> verify(authenticationService).authenticateTrainer("Minerva.McGonagall", "password"),
        () -> verify(trainerMapper).updateFromRequest(request, authenticatedTrainer),
        () -> verify(trainingTypeDao).findByName("Yoga"),
        () -> verify(trainerDao).save(authenticatedTrainer),
        () -> verify(trainerMapper).toProfileResponse(authenticatedTrainer),
        () -> verifyNoMoreInteractions(trainerDao));
  }

  @Test
  void updateShouldThrowAuthenticationExceptionWhenTrainerDoesNotExist() {
    UpdateTrainerRequest request = updateTrainerRequest(22L, "Minnie", "McGonagall", "Yoga");
    AuthenticationException exception = new AuthenticationException("Invalid username or password");
    when(authenticationService.authenticateTrainer("Minerva.McGonagall", "password"))
        .thenThrow(exception);

    assertThatThrownBy(() -> trainerService.update(request)).isSameAs(exception);

    assertAll(() -> verifyNoInteractions(trainingTypeDao), () -> verifyNoInteractions(trainerDao));
  }

  @Test
  void updateShouldThrowRuntimeExceptionWhenDaoFails() {
    UpdateTrainerRequest request = updateTrainerRequest(22L, "Minnie", "McGonagall", "Fitness");
    Trainer authenticatedTrainer = trainer(22L, "Minerva", "McGonagall", "Minerva.McGonagall");
    RuntimeException exception = new RuntimeException("DAO failure");
    when(authenticationService.authenticateTrainer("Minerva.McGonagall", "password"))
        .thenReturn(authenticatedTrainer);
    when(trainingTypeDao.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
    doThrow(exception).when(trainerDao).save(authenticatedTrainer);

    assertThatThrownBy(() -> trainerService.update(request)).isSameAs(exception);
  }

  @Test
  void updateShouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
    assertThatThrownBy(() -> trainerService.update(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Update trainer request must not be null");
  }

  private static CreateTrainerRequest createTrainerRequest(
      String firstName, String lastName, String specialization, Boolean active) {
    return new CreateTrainerRequest(firstName, lastName, specialization);
  }

  private static UpdateTrainerRequest updateTrainerRequest(
      Long id, String firstName, String lastName, String specialization) {
    return new UpdateTrainerRequest(
        "Minerva.McGonagall", "password", firstName, lastName, specialization, true);
  }

  private static TrainerProfileResponse trainerProfileResponse(
      Long id, String username, String firstName, String lastName, String specialization) {
    return new TrainerProfileResponse(username, firstName, lastName, true, specialization);
  }

  private static TrainerSummaryResponse trainerSummaryResponse(
      Long id, String username, String firstName, String lastName) {
    return new TrainerSummaryResponse(username, firstName, lastName, "Fitness");
  }
}
