package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Test
    void createShouldGenerateUsernameAndPassword() {
        Trainee trainee = Trainee.builder()
                .user(user("John", "Doe", null))
                .build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(usernameGenerator.generate("John", "Doe", Collections.emptySet())).thenReturn("John.Doe");

        traineeService.create(trainee);

        assertAll(
                () -> assertThat(trainee.getUser().getUsername()).isEqualTo("John.Doe"),
                () -> assertThat(trainee.getUser().getPassword()).isEqualTo("Passw0rd12"),
                () -> verify(usernameGenerator).generate("John", "Doe", Collections.emptySet()),
                () -> verify(passwordGenerator).generate(),
                () -> verify(traineeDao).save(trainee)
        );
    }

    @Test
    void createShouldPassExistingUsernamesToGenerator() {
        Trainee newTrainee = Trainee.builder()
                .user(user("John", "Doe", null))
                .build();
        Trainee existingBase = trainee("Jane", "Base", "John.Doe");
        Trainee existing2 = trainee("Jane", "Second", "John.Doe2");
        Trainee similarName = trainee("Jane", "Similar", "John.Doering");
        Trainee withoutUser = Trainee.builder().build();
        Trainee withoutUsername = Trainee.builder()
                .user(user("Jane", "Null", null))
                .build();

        Set<String> existingUsernames = Set.of("John.Doe", "John.Doe2", "John.Doering");
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(
                existingBase,
                existing2,
                similarName,
                withoutUser,
                withoutUsername
        ));
        when(usernameGenerator.generate("John", "Doe", existingUsernames)).thenReturn("John.Doe1");

        traineeService.create(newTrainee);

        assertAll(
                () -> assertThat(newTrainee.getUser().getUsername()).isEqualTo("John.Doe1"),
                () -> verify(usernameGenerator).generate("John", "Doe", existingUsernames),
                () -> verify(traineeDao).save(newTrainee)
        );
    }

    @Test
    void createShouldThrowRuntimeExceptionWhenDaoFails() {
        Trainee trainee = Trainee.builder()
                .user(user("John", "Doe", null))
                .build();
        RuntimeException exception = new RuntimeException("DAO failure");

        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(usernameGenerator.generate("John", "Doe", Collections.emptySet())).thenReturn("John.Doe");
        doThrow(exception).when(traineeDao).save(trainee);

        assertThatThrownBy(() -> traineeService.create(trainee))
                .isSameAs(exception);
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenTraineeIsNull() {
        assertThatThrownBy(() -> traineeService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee must not be null");
    }

    @Test
    void createShouldThrowIllegalArgumentExceptionWhenUserIsNull() {
        Trainee trainee = Trainee.builder().build();

        assertThatThrownBy(() -> traineeService.create(trainee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee user must not be null");
    }

    @Test
    void updateShouldSaveTraineeChanges() {
        Trainee trainee = trainee(10L, "Hermione", "Granger", "Hermione.Granger");
        when(traineeDao.findById(10L)).thenReturn(Optional.of(trainee));

        traineeService.update(trainee);

        assertAll(
                () -> verify(traineeDao).findById(10L),
                () -> verify(traineeDao).save(trainee),
                () -> verifyNoMoreInteractions(traineeDao)
        );
    }

    @Test
    void updateShouldThrowEntityNotFoundExceptionWhenTraineeDoesNotExist() {
        Trainee trainee = trainee(10L, "Hermione", "Granger", "Hermione.Granger");
        when(traineeDao.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.update(trainee))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee profile not found");
    }

    @Test
    void updateShouldThrowRuntimeExceptionWhenDaoFails() {
        Trainee trainee = trainee(10L, "Hermione", "Granger", "Hermione.Granger");
        RuntimeException exception = new RuntimeException("DAO failure");
        when(traineeDao.findById(10L)).thenReturn(Optional.of(trainee));
        doThrow(exception).when(traineeDao).save(trainee);

        assertThatThrownBy(() -> traineeService.update(trainee))
                .isSameAs(exception);
    }

    @Test
    void updateShouldThrowIllegalArgumentExceptionWhenTraineeIsNull() {
        assertThatThrownBy(() -> traineeService.update(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee must not be null");
    }

    @Test
    void updateShouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        Trainee trainee = trainee("Hermione", "Granger", "Hermione.Granger");

        assertThatThrownBy(() -> traineeService.update(trainee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee id must not be null");
    }

    @Test
    void deleteShouldDelegateToDao() {
        traineeService.delete(15L);

        verify(traineeDao).delete(15L);
        verifyNoMoreInteractions(traineeDao);
    }

    @Test
    void deleteShouldThrowRuntimeExceptionWhenDaoFails() {
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(traineeDao).delete(15L);

        assertThatThrownBy(() -> traineeService.delete(15L))
                .isSameAs(exception);
    }

    @Test
    void deleteShouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> traineeService.delete(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee id must not be null");
    }

    @Test
    void findByIdShouldReturnTraineeWhenTraineeExists() {
        Trainee trainee = trainee(100L, "Ron", "Weasley", "Ron.Weasley");
        when(traineeDao.findById(100L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.findById(100L);

        assertAll(
                () -> assertThat(result).isSameAs(trainee),
                () -> assertThat(result.getUser().getFirstName()).isEqualTo("Ron"),
                () -> verify(traineeDao).findById(100L)
        );
    }

    @Test
    void findByIdShouldThrowEntityNotFoundExceptionWhenTraineeDoesNotExist() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee profile not found");

        verify(traineeDao).findById(999L);
    }

    @Test
    void findByIdShouldThrowRuntimeExceptionWhenDaoFails() {
        RuntimeException exception = new RuntimeException("DAO failure");
        when(traineeDao.findById(15L)).thenThrow(exception);

        assertThatThrownBy(() -> traineeService.findById(15L))
                .isSameAs(exception);
    }

    @Test
    void findByIdShouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> traineeService.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee id must not be null");
    }
}
