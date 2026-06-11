package com.epam.gymcrm.service;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.service.impl.AuthenticationServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

  @InjectMocks private AuthenticationServiceImpl authenticationService;

  @Mock private TraineeRepository traineeRepository;

  @Mock private TrainerRepository trainerRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Test
  void authenticateTraineeShouldReturnTraineeWhenCredentialsMatch() {
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    when(traineeRepository.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainee));
    when(passwordEncoder.matches("password", "password")).thenReturn(true);

    Trainee result = authenticationService.authenticateTrainee("Jane.Doe", "password");

    assertAll(
        () -> assertThat(result).isSameAs(trainee),
        () -> verify(traineeRepository).findByUsername("Jane.Doe"),
        () -> verifyNoInteractions(trainerRepository));
  }

  @Test
  void authenticateTraineeShouldThrowAuthenticationExceptionWhenUsernameDoesNotExist() {
    when(traineeRepository.findByUsername("Unknown.Trainee")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> authenticationService.authenticateTrainee("Unknown.Trainee", "password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid username or password");

    verify(traineeRepository).findByUsername("Unknown.Trainee");
    verifyNoInteractions(trainerRepository);
  }

  @Test
  void authenticateTraineeShouldThrowAuthenticationExceptionWhenPasswordDoesNotMatch() {
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    when(traineeRepository.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainee));
    when(passwordEncoder.matches("wrong-password", "password")).thenReturn(false);

    assertThatThrownBy(
            () -> authenticationService.authenticateTrainee("Jane.Doe", "wrong-password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid username or password");

    verify(traineeRepository).findByUsername("Jane.Doe");
    verifyNoInteractions(trainerRepository);
  }

  @Test
  void authenticateTraineeShouldThrowIllegalArgumentExceptionWhenUsernameIsNull() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainee(null, "password"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username must not be blank");

    verifyNoInteractions(traineeRepository, trainerRepository);
  }

  @Test
  void authenticateTraineeShouldThrowIllegalArgumentExceptionWhenUsernameIsBlank() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainee(" ", "password"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username must not be blank");

    verifyNoInteractions(traineeRepository, trainerRepository);
  }

  @Test
  void authenticateTraineeShouldThrowIllegalArgumentExceptionWhenPasswordIsNull() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainee("Jane.Doe", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password must not be blank");

    verifyNoInteractions(traineeRepository, trainerRepository);
  }

  @Test
  void authenticateTraineeShouldThrowIllegalArgumentExceptionWhenPasswordIsBlank() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainee("Jane.Doe", " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password must not be blank");

    verifyNoInteractions(traineeRepository, trainerRepository);
  }

  @Test
  void authenticateTrainerShouldReturnTrainerWhenCredentialsMatch() {
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(trainerRepository.findByUsername("John.Coach")).thenReturn(Optional.of(trainer));
    when(passwordEncoder.matches("password", "password")).thenReturn(true);

    Trainer result = authenticationService.authenticateTrainer("John.Coach", "password");

    assertAll(
        () -> assertThat(result).isSameAs(trainer),
        () -> verify(trainerRepository).findByUsername("John.Coach"),
        () -> verifyNoInteractions(traineeRepository));
  }

  @Test
  void authenticateTrainerShouldThrowAuthenticationExceptionWhenUsernameDoesNotExist() {
    when(trainerRepository.findByUsername("Unknown.Trainer")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> authenticationService.authenticateTrainer("Unknown.Trainer", "password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid username or password");

    verify(trainerRepository).findByUsername("Unknown.Trainer");
    verifyNoInteractions(traineeRepository);
  }

  @Test
  void authenticateTrainerShouldThrowAuthenticationExceptionWhenPasswordDoesNotMatch() {
    Trainer trainer = trainer("John", "Coach", "John.Coach");
    when(trainerRepository.findByUsername("John.Coach")).thenReturn(Optional.of(trainer));
    when(passwordEncoder.matches("wrong-password", "password")).thenReturn(false);

    assertThatThrownBy(
            () -> authenticationService.authenticateTrainer("John.Coach", "wrong-password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid username or password");

    verify(trainerRepository).findByUsername("John.Coach");
    verifyNoInteractions(traineeRepository);
  }

  @Test
  void authenticateTrainerShouldThrowIllegalArgumentExceptionWhenUsernameIsNull() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainer(null, "password"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username must not be blank");

    verifyNoInteractions(traineeRepository, trainerRepository);
  }

  @Test
  void authenticateTrainerShouldThrowIllegalArgumentExceptionWhenUsernameIsBlank() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainer(" ", "password"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username must not be blank");

    verifyNoInteractions(traineeRepository, trainerRepository);
  }

  @Test
  void authenticateTrainerShouldThrowIllegalArgumentExceptionWhenPasswordIsNull() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainer("John.Coach", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password must not be blank");

    verifyNoInteractions(traineeRepository, trainerRepository);
  }

  @Test
  void authenticateTrainerShouldThrowIllegalArgumentExceptionWhenPasswordIsBlank() {
    assertThatThrownBy(() -> authenticationService.authenticateTrainer("John.Coach", " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password must not be blank");

    verifyNoInteractions(traineeRepository, trainerRepository);
  }
}
