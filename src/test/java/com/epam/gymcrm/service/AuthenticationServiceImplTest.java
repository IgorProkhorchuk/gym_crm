package com.epam.gymcrm.service;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.impl.AuthenticationServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

  @InjectMocks private AuthenticationServiceImpl authenticationService;

  @Mock private TraineeDao traineeDao;

  @Mock private TrainerDao trainerDao;

  @Test
  void authenticateTraineeShouldReturnTraineeWhenCredentialsMatch() {
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    when(traineeDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

    Trainee result = authenticationService.authenticateTrainee("Jane.Doe", "password");

    assertAll(
        () -> assertThat(result).isSameAs(trainee),
        () -> verify(traineeDao).findByUsername("Jane.Doe"),
        () -> verifyNoInteractions(trainerDao));
  }

  @Test
  void authenticateTraineeShouldThrowAuthenticationExceptionWhenUsernameDoesNotExist() {
    when(traineeDao.findByUsername("Unknown.Trainee")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> authenticationService.authenticateTrainee("Unknown.Trainee", "password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid username or password");

    verify(traineeDao).findByUsername("Unknown.Trainee");
    verifyNoInteractions(trainerDao);
  }

  @Test
  void authenticateTraineeShouldThrowAuthenticationExceptionWhenPasswordDoesNotMatch() {
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    when(traineeDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainee));

    assertThatThrownBy(
            () -> authenticationService.authenticateTrainee("Jane.Doe", "wrong-password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid username or password");

    verify(traineeDao).findByUsername("Jane.Doe");
    verifyNoInteractions(trainerDao);
  }

  @Test
  void authenticateTraineeShouldThrowIllegalArgumentExceptionWhenUsernameIsNull() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainee(null, "password"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username must not be blank");

    verifyNoInteractions(traineeDao, trainerDao);
  }

  @Test
  void authenticateTraineeShouldThrowIllegalArgumentExceptionWhenUsernameIsBlank() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainee(" ", "password"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username must not be blank");

    verifyNoInteractions(traineeDao, trainerDao);
  }

  @Test
  void authenticateTraineeShouldThrowIllegalArgumentExceptionWhenPasswordIsNull() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainee("Jane.Doe", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password must not be blank");

    verifyNoInteractions(traineeDao, trainerDao);
  }

  @Test
  void authenticateTraineeShouldThrowIllegalArgumentExceptionWhenPasswordIsBlank() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainee("Jane.Doe", " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password must not be blank");

    verifyNoInteractions(traineeDao, trainerDao);
  }

  @Test
  void authenticateTrainerShouldReturnTrainerWhenCredentialsMatch() {
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(trainerDao.findByUsername("John.Coach")).thenReturn(Optional.of(trainer));

    Trainer result = authenticationService.authenticateTrainer("John.Coach", "password");

    assertAll(
        () -> assertThat(result).isSameAs(trainer),
        () -> verify(trainerDao).findByUsername("John.Coach"),
        () -> verifyNoInteractions(traineeDao));
  }

  @Test
  void authenticateTrainerShouldThrowAuthenticationExceptionWhenUsernameDoesNotExist() {
    when(trainerDao.findByUsername("Unknown.Trainer")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> authenticationService.authenticateTrainer("Unknown.Trainer", "password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid username or password");

    verify(trainerDao).findByUsername("Unknown.Trainer");
    verifyNoInteractions(traineeDao);
  }

  @Test
  void authenticateTrainerShouldThrowAuthenticationExceptionWhenPasswordDoesNotMatch() {
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(trainerDao.findByUsername("John.Coach")).thenReturn(Optional.of(trainer));

    assertThatThrownBy(
            () -> authenticationService.authenticateTrainer("John.Coach", "wrong-password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid username or password");

    verify(trainerDao).findByUsername("John.Coach");
    verifyNoInteractions(traineeDao);
  }

  @Test
  void authenticateTrainerShouldThrowIllegalArgumentExceptionWhenUsernameIsNull() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainer(null, "password"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username must not be blank");

    verifyNoInteractions(traineeDao, trainerDao);
  }

  @Test
  void authenticateTrainerShouldThrowIllegalArgumentExceptionWhenUsernameIsBlank() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainer(" ", "password"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username must not be blank");

    verifyNoInteractions(traineeDao, trainerDao);
  }

  @Test
  void authenticateTrainerShouldThrowIllegalArgumentExceptionWhenPasswordIsNull() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainer("John.Coach", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password must not be blank");

    verifyNoInteractions(traineeDao, trainerDao);
  }

  @Test
  void authenticateTrainerShouldThrowIllegalArgumentExceptionWhenPasswordIsBlank() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainer("John.Coach", " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password must not be blank");

    verifyNoInteractions(traineeDao, trainerDao);
  }
}
