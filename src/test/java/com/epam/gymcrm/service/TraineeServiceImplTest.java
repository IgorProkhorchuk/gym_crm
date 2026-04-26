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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Test
    void testCreateTraineeGeneratesUsernameAndPassword() {
        Trainee trainee = Trainee.builder().firstName("John").lastName("Doe").build();
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());

        traineeService.create(trainee);

        assertAll(
                () -> assertThat(trainee.getUsername()).isEqualTo("John.Doe"),
                () -> assertThat(trainee.getPassword()).isEqualTo("Passw0rd12"),
                () -> verify(passwordGenerator).generate(),
                () -> verify(traineeDao).save(trainee)
        );
    }

    @Test
    void testCreateTraineeWithExistingUsernameGeneratesSuffix() {
        Trainee newTrainee = Trainee.builder().firstName("John").lastName("Doe").build();
        Trainee existingTrainee = Trainee.builder().username("John.Doe").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee));

        traineeService.create(newTrainee);

        assertThat(newTrainee.getUsername()).isEqualTo("John.Doe1");
    }

    @Test
    void testCreateTraineeFillsTheGapInUsernames() {
        Trainee newTrainee = Trainee.builder().firstName("John").lastName("Doe").build();

        Trainee existingBase = Trainee.builder().username("John.Doe").build();
        Trainee existing2 = Trainee.builder().username("John.Doe2").build();
        Trainee similarName = Trainee.builder().username("John.Doering").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(existingBase, existing2, similarName));

        traineeService.create(newTrainee);

        assertThat(newTrainee.getUsername()).isEqualTo("John.Doe1");
    }

    @Test
    void testFindByIdReturnsTrainee() {
        Trainee trainee = Trainee.builder().userId(100L).firstName("Ron").build();
        when(traineeDao.findById(100L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.findById(100L);

        assertAll(
                () -> assertThat(result).isSameAs(trainee),
                () -> assertThat(result.getFirstName()).isEqualTo("Ron"),
                () -> verify(traineeDao).findById(100L)
        );
    }

    @Test
    void testFindByIdThrowsWhenNotFound() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee profile not found");

        verify(traineeDao).findById(999L);
    }

    @Test
    void testCreateTraineeRethrowsDaoFailure() {
        Trainee trainee = Trainee.builder().userId(20L).firstName("John").lastName("Doe").build();
        RuntimeException exception = new RuntimeException("DAO failure");

        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        doThrow(exception).when(traineeDao).save(trainee);

        assertThatThrownBy(() -> traineeService.create(trainee))
                .isSameAs(exception);
    }

    @Test
    void testCreateTraineeRejectsNullTrainee() {
        assertThatThrownBy(() -> traineeService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee must not be null");
    }

    @Test
    void testCreateTraineeSkipsTakenSequentialSuffixes() {
        Trainee newTrainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        Trainee existingBase = Trainee.builder().username("John.Doe").build();
        Trainee existing1 = Trainee.builder().username("John.Doe1").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(existingBase, existing1));

        traineeService.create(newTrainee);

        assertAll(
                () -> assertThat(newTrainee.getUsername()).isEqualTo("John.Doe2"),
                () -> assertThat(newTrainee.getPassword()).isEqualTo("Passw0rd12"),
                () -> verify(passwordGenerator).generate(),
                () -> verify(traineeDao).save(newTrainee)
        );
    }

    @Test
    void testUpdateTraineeDelegatesToDao() {
        Trainee trainee = Trainee.builder()
                .userId(10L)
                .firstName("Hermione")
                .lastName("Granger")
                .username("Hermione.Granger")
                .build();

        traineeService.update(trainee);

        verify(traineeDao).save(trainee);
        verifyNoMoreInteractions(traineeDao);
    }

    @Test
    void testUpdateTraineeRethrowsDaoFailure() {
        Trainee trainee = Trainee.builder().userId(10L).build();
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(traineeDao).save(trainee);

        assertThatThrownBy(() -> traineeService.update(trainee))
                .isSameAs(exception);
    }

    @Test
    void testDeleteTraineeDelegatesToDao() {
        traineeService.delete(15L);

        verify(traineeDao).delete(15L);
        verifyNoMoreInteractions(traineeDao);
    }

    @Test
    void testDeleteTraineeRethrowsDaoFailure() {
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(traineeDao).delete(15L);

        assertThatThrownBy(() -> traineeService.delete(15L))
                .isSameAs(exception);
    }

    @Test
    void testFindTraineeByIdRethrowsDaoFailure() {
        RuntimeException exception = new RuntimeException("DAO failure");
        when(traineeDao.findById(15L)).thenThrow(exception);

        assertThatThrownBy(() -> traineeService.findById(15L))
                .isSameAs(exception);
    }

    @Test
    void testFindTraineeByIdRejectsNullId() {
        assertThatThrownBy(() -> traineeService.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainee id must not be null");
    }

}
