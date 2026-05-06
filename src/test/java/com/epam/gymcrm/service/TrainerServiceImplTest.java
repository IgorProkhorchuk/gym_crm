package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ProfileStateException;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Test
    void createShouldGenerateUsernameAndPassword() {
        Trainer trainer = Trainer.builder()
                .user(user("Severus", "Snape", null))
                .specialization(trainingType("Fitness"))
                .build();

        when(trainingTypeDao.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());
        when(usernameGenerator.generate("Severus", "Snape", Collections.emptySet())).thenReturn("Severus.Snape");

        trainerService.create(trainer);

        assertAll(
                () -> assertThat(trainer.getUser().getUsername()).isEqualTo("Severus.Snape"),
                () -> assertThat(trainer.getUser().getPassword()).isEqualTo("Passw0rd12"),
                () -> verify(usernameGenerator).generate("Severus", "Snape", Collections.emptySet()),
                () -> verify(passwordGenerator).generate(),
                () -> verify(trainerDao).save(trainer)
        );
    }

    @Test
    void createShouldPassExistingUsernamesToGenerator() {
        Trainer newTrainer = Trainer.builder()
                .user(user("Severus", "Snape", null))
                .specialization(trainingType("Fitness"))
                .build();
        Trainer existingBase = trainer("Jane", "Base", "Severus.Snape");
        Trainer existing2 = trainer("Jane", "Second", "Severus.Snape2");
        Trainer similarName = trainer("Jane", "Similar", "Severus.Snapely");
        Trainer withoutUser = Trainer.builder().build();
        Trainer withoutUsername = Trainer.builder()
                .user(user("Jane", "Null", null))
                .build();

        Set<String> existingUsernames = Set.of("Severus.Snape", "Severus.Snape2", "Severus.Snapely");
        when(trainingTypeDao.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(List.of(
                existingBase,
                existing2,
                similarName,
                withoutUser,
                withoutUsername
        ));
        when(usernameGenerator.generate("Severus", "Snape", existingUsernames)).thenReturn("Severus.Snape1");

        trainerService.create(newTrainer);

        assertAll(
                () -> assertThat(newTrainer.getUser().getUsername()).isEqualTo("Severus.Snape1"),
                () -> verify(usernameGenerator).generate("Severus", "Snape", existingUsernames),
                () -> verify(trainerDao).save(newTrainer)
        );
    }

    @Test
    void createShouldThrowRuntimeExceptionWhenDaoFails() {
        Trainer trainer = Trainer.builder()
                .user(user("Severus", "Snape", null))
                .specialization(trainingType("Fitness"))
                .build();
        RuntimeException exception = new RuntimeException("DAO failure");

        when(trainingTypeDao.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(usernameGenerator.generate("Severus", "Snape", Collections.emptySet()))
                .thenReturn("Severus.Snape");
        doThrow(exception).when(trainerDao).save(trainer);

        assertThatThrownBy(() -> trainerService.create(trainer))
                .isSameAs(exception);
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenTrainerIsNull() {
        assertThatThrownBy(() -> trainerService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer must not be null");
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenUserIsNull() {
        Trainer trainer = Trainer.builder().build();

        assertThatThrownBy(() -> trainerService.create(trainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer user must not be null");
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenFirstNameIsBlank() {
        Trainer trainer = Trainer.builder()
                .user(user(" ", "Snape", null))
                .specialization(trainingType("Fitness"))
                .build();

        assertThatThrownBy(() -> trainerService.create(trainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name must not be blank");

        assertAll(
                () -> verifyNoInteractions(trainingTypeDao),
                () -> verifyNoInteractions(usernameGenerator),
                () -> verifyNoInteractions(passwordGenerator),
                () -> verifyNoInteractions(trainerDao)
        );
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenLastNameIsBlank() {
        Trainer trainer = Trainer.builder()
                .user(user("Severus", " ", null))
                .specialization(trainingType("Fitness"))
                .build();

        assertThatThrownBy(() -> trainerService.create(trainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Last name must not be blank");

        assertAll(
                () -> verifyNoInteractions(trainingTypeDao),
                () -> verifyNoInteractions(usernameGenerator),
                () -> verifyNoInteractions(passwordGenerator),
                () -> verifyNoInteractions(trainerDao)
        );
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenActiveIsNull() {
        Trainer trainer = Trainer.builder()
                .user(user("Severus", "Snape", null))
                .specialization(trainingType("Fitness"))
                .build();
        trainer.getUser().setActive(null);

        assertThatThrownBy(() -> trainerService.create(trainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Active must not be null");

        assertAll(
                () -> verifyNoInteractions(trainingTypeDao),
                () -> verifyNoInteractions(usernameGenerator),
                () -> verifyNoInteractions(passwordGenerator),
                () -> verifyNoInteractions(trainerDao)
        );
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenSpecializationIsNull() {
        Trainer trainer = Trainer.builder()
                .user(user("Severus", "Snape", null))
                .build();

        assertThatThrownBy(() -> trainerService.create(trainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer specialization must not be null");
    }

    @Test
    void createShouldThrowEntityNotFoundExceptionWhenSpecializationDoesNotExist() {
        Trainer trainer = Trainer.builder()
                .user(user("Severus", "Snape", null))
                .specialization(trainingType("Unknown"))
                .build();
        when(trainingTypeDao.findByName("Unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.create(trainer))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Training type not found");
    }

    @Test
    void getProfileShouldReturnAuthenticatedTrainer() {
        Trainer trainer = trainer("John", "Coach", "John.Coach");
        when(authenticationService.authenticateTrainer("John.Coach", "password")).thenReturn(trainer);

        Trainer result = trainerService.getProfile("John.Coach", "password");

        assertAll(
                () -> assertThat(result).isSameAs(trainer),
                () -> verify(authenticationService).authenticateTrainer("John.Coach", "password"),
                () -> verifyNoMoreInteractions(authenticationService)
        );
    }

    @Test
    void changePasswordShouldUpdateAuthenticatedTrainerPassword() {
        Trainer trainer = trainer("John", "Coach", "John.Coach");
        when(authenticationService.authenticateTrainer("John.Coach", "old-password")).thenReturn(trainer);

        trainerService.changePassword("John.Coach", "old-password", "new-password");

        assertAll(
                () -> assertThat(trainer.getUser().getPassword()).isEqualTo("new-password"),
                () -> verify(authenticationService).authenticateTrainer("John.Coach", "old-password"),
                () -> verify(trainerDao).save(trainer)
        );
    }

    @Test
    void changePasswordShouldThrowIllegalArgumentExceptionWhenNewPasswordIsNull() {
        assertThatThrownBy(() -> trainerService.changePassword("John.Coach", "old-password", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password must not be blank");
    }

    @Test
    void changePasswordShouldThrowIllegalArgumentExceptionWhenNewPasswordIsBlank() {
        assertThatThrownBy(() -> trainerService.changePassword("John.Coach", "old-password", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password must not be blank");
    }

    @Test
    void activateShouldSetAuthenticatedTrainerActiveWhenCurrentlyInactive() {
        Trainer trainer = trainer("John", "Coach", "John.Coach");
        trainer.getUser().setActive(false);
        when(authenticationService.authenticateTrainer("John.Coach", "password")).thenReturn(trainer);

        trainerService.activate("John.Coach", "password");

        assertAll(
                () -> assertThat(trainer.getUser().getActive()).isTrue(),
                () -> verify(authenticationService).authenticateTrainer("John.Coach", "password"),
                () -> verify(trainerDao).save(trainer)
        );
    }

    @Test
    void activateShouldThrowProfileStateExceptionWhenTrainerAlreadyActive() {
        Trainer trainer = trainer("John", "Coach", "John.Coach");
        when(authenticationService.authenticateTrainer("John.Coach", "password")).thenReturn(trainer);

        assertThatThrownBy(() -> trainerService.activate("John.Coach", "password"))
                .isInstanceOf(ProfileStateException.class)
                .hasMessage("Trainer profile is already active");

        assertAll(
                () -> verify(authenticationService).authenticateTrainer("John.Coach", "password"),
                () -> verifyNoMoreInteractions(trainerDao)
        );
    }

    @Test
    void deactivateShouldSetAuthenticatedTrainerInactiveWhenCurrentlyActive() {
        Trainer trainer = trainer("John", "Coach", "John.Coach");
        when(authenticationService.authenticateTrainer("John.Coach", "password")).thenReturn(trainer);

        trainerService.deactivate("John.Coach", "password");

        assertAll(
                () -> assertThat(trainer.getUser().getActive()).isFalse(),
                () -> verify(authenticationService).authenticateTrainer("John.Coach", "password"),
                () -> verify(trainerDao).save(trainer)
        );
    }

    @Test
    void deactivateShouldThrowProfileStateExceptionWhenTrainerAlreadyInactive() {
        Trainer trainer = trainer("John", "Coach", "John.Coach");
        trainer.getUser().setActive(false);
        when(authenticationService.authenticateTrainer("John.Coach", "password")).thenReturn(trainer);

        assertThatThrownBy(() -> trainerService.deactivate("John.Coach", "password"))
                .isInstanceOf(ProfileStateException.class)
                .hasMessage("Trainer profile is already inactive");

        assertAll(
                () -> verify(authenticationService).authenticateTrainer("John.Coach", "password"),
                () -> verifyNoMoreInteractions(trainerDao)
        );
    }

    @Test
    void getUnassignedTrainersShouldAuthenticateTraineeAndReturnDaoResult() {
        Trainer trainer = trainer("Available", "Trainer", "Available.Trainer");
        when(authenticationService.authenticateTrainee("Jane.Doe", "password"))
                .thenReturn(com.epam.gymcrm.TestFixtures.trainee("Jane", "Doe", "Jane.Doe"));
        when(trainerDao.findNotAssignedToTrainee("Jane.Doe")).thenReturn(List.of(trainer));

        List<Trainer> result = trainerService.getUnassignedTrainers("Jane.Doe", "password");

        assertAll(
                () -> assertThat(result).containsExactly(trainer),
                () -> verify(authenticationService).authenticateTrainee("Jane.Doe", "password"),
                () -> verify(trainerDao).findNotAssignedToTrainee("Jane.Doe")
        );
    }

    @Test
    void updateShouldSaveAuthenticatedTrainerChanges() {
        Trainer authenticatedTrainer = trainer(22L, "Minerva", "McGonagall", "Minerva.McGonagall");
        Trainer updatedTrainer = trainer(22L, "Minnie", "McGonagall", "Changed.Username", trainingType("Yoga"));
        updatedTrainer.getUser().setPassword("changed-password");
        updatedTrainer.getUser().setActive(false);
        when(authenticationService.authenticateTrainer("Minerva.McGonagall", "password")).thenReturn(authenticatedTrainer);
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(trainingType("Yoga")));

        trainerService.update("Minerva.McGonagall", "password", updatedTrainer);

        assertAll(
                () -> assertThat(authenticatedTrainer.getUser().getFirstName()).isEqualTo("Minnie"),
                () -> assertThat(authenticatedTrainer.getUser().getLastName()).isEqualTo("McGonagall"),
                () -> assertThat(authenticatedTrainer.getUser().getUsername()).isEqualTo("Minerva.McGonagall"),
                () -> assertThat(authenticatedTrainer.getUser().getPassword()).isEqualTo("password"),
                () -> assertThat(authenticatedTrainer.getUser().getActive()).isTrue(),
                () -> assertThat(authenticatedTrainer.getSpecialization().getTrainingTypeName()).isEqualTo("Yoga"),
                () -> verify(authenticationService).authenticateTrainer("Minerva.McGonagall", "password"),
                () -> verify(trainingTypeDao).findByName("Yoga"),
                () -> verify(trainerDao).save(authenticatedTrainer),
                () -> verifyNoMoreInteractions(trainerDao)
        );
    }

    @Test
    void updateShouldThrowAuthenticationExceptionWhenTrainerDoesNotExist() {
        Trainer trainer = trainer(22L, "Minerva", "McGonagall", "Minerva.McGonagall");
        AuthenticationException exception = new AuthenticationException("Invalid username or password");
        when(authenticationService.authenticateTrainer("Minerva.McGonagall", "password")).thenThrow(exception);

        assertThatThrownBy(() -> trainerService.update("Minerva.McGonagall", "password", trainer))
                .isSameAs(exception);

        assertAll(
                () -> verifyNoInteractions(trainingTypeDao),
                () -> verifyNoInteractions(trainerDao)
        );
    }

    @Test
    void updateShouldThrowRuntimeExceptionWhenDaoFails() {
        Trainer authenticatedTrainer = trainer(22L, "Minerva", "McGonagall", "Minerva.McGonagall");
        Trainer updatedTrainer = trainer(22L, "Minnie", "McGonagall", "Minerva.McGonagall");
        RuntimeException exception = new RuntimeException("DAO failure");
        when(authenticationService.authenticateTrainer("Minerva.McGonagall", "password")).thenReturn(authenticatedTrainer);
        when(trainingTypeDao.findByName("Fitness")).thenReturn(Optional.of(trainingType("Fitness")));
        doThrow(exception).when(trainerDao).save(authenticatedTrainer);

        assertThatThrownBy(() -> trainerService.update("Minerva.McGonagall", "password", updatedTrainer))
                .isSameAs(exception);
    }

    @Test
    void updateShouldThrowIllegalArgumentExceptionWhenTrainerIsNull() {
        assertThatThrownBy(() -> trainerService.update("Minerva.McGonagall", "password", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer must not be null");
    }

    @Test
    void updateShouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        Trainer trainer = trainer("Minerva", "McGonagall", "Minerva.McGonagall");

        assertThatThrownBy(() -> trainerService.update("Minerva.McGonagall", "password", trainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer id must not be null");
    }

    @Test
    void updateShouldThrowAuthenticationExceptionWhenAuthenticatedTrainerDoesNotMatchPayload() {
        Trainer authenticatedTrainer = trainer(23L, "Minerva", "McGonagall", "Minerva.McGonagall");
        Trainer updatedTrainer = trainer(22L, "Minnie", "McGonagall", "Minerva.McGonagall");
        when(authenticationService.authenticateTrainer("Minerva.McGonagall", "password")).thenReturn(authenticatedTrainer);

        assertThatThrownBy(() -> trainerService.update("Minerva.McGonagall", "password", updatedTrainer))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Authenticated trainer does not match updated profile");

        assertAll(
                () -> verifyNoInteractions(trainingTypeDao),
                () -> verifyNoInteractions(trainerDao)
        );
    }

}
