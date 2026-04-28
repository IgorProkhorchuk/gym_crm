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

    @Mock
    private UsernameGenerator usernameGenerator;

    @Test
    void createShouldGenerateUsernameAndPassword() {
        Trainee trainee = Trainee.builder().firstName("John").lastName("Doe").build();
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(usernameGenerator.generate("John", "Doe", Collections.emptySet())).thenReturn("John.Doe");

        traineeService.create(trainee);

        assertAll(
                () -> assertThat(trainee.getUsername()).isEqualTo("John.Doe"),
                () -> assertThat(trainee.getPassword()).isEqualTo("Passw0rd12"),
                () -> verify(usernameGenerator).generate("John", "Doe", Collections.emptySet()),
                () -> verify(passwordGenerator).generate(),
                () -> verify(traineeDao).save(trainee)
        );
    }

    @Test
    void createShouldUseGeneratedUsernameWithSuffixWhenUsernameExists() {
        Trainee newTrainee = Trainee.builder().firstName("John").lastName("Doe").build();
        Trainee existingTrainee = Trainee.builder().username("John.Doe").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee));
        when(usernameGenerator.generate("John", "Doe", Set.of("John.Doe"))).thenReturn("John.Doe1");

        traineeService.create(newTrainee);

        assertAll(
                () -> assertThat(newTrainee.getUsername()).isEqualTo("John.Doe1"),
                () -> verify(usernameGenerator).generate("John", "Doe", Set.of("John.Doe"))
        );
    }

    @Test
    void createShouldPassExistingUsernamesToGenerator() {
        Trainee newTrainee = Trainee.builder().firstName("John").lastName("Doe").build();

        Trainee existingBase = Trainee.builder().username("John.Doe").build();
        Trainee existing2 = Trainee.builder().username("John.Doe2").build();
        Trainee similarName = Trainee.builder().username("John.Doering").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(existingBase, existing2, similarName));
        when(usernameGenerator.generate("John", "Doe", Set.of("John.Doe", "John.Doe2", "John.Doering")))
                .thenReturn("John.Doe1");

        traineeService.create(newTrainee);

        assertAll(
                () -> assertThat(newTrainee.getUsername()).isEqualTo("John.Doe1"),
                () -> verify(usernameGenerator).generate(
                        "John",
                        "Doe",
                        Set.of("John.Doe", "John.Doe2", "John.Doering")
                )
        );
    }

    @Test
    void findByIdShouldReturnTraineeWhenTraineeExists() {
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
    void findByIdShouldThrowEntityNotFoundExceptionWhenTraineeDoesNotExist() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee profile not found");

        verify(traineeDao).findById(999L);
    }

    @Test
    void createShouldThrowRuntimeExceptionWhenDaoFails() {
        Trainee trainee = Trainee.builder().userId(20L).firstName("John").lastName("Doe").build();
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
    void createShouldUseGeneratedUsernameWithNextSuffixWhenSequentialSuffixesExist() {
        Trainee newTrainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        Trainee existingBase = Trainee.builder().username("John.Doe").build();
        Trainee existing1 = Trainee.builder().username("John.Doe1").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(traineeDao.findAll()).thenReturn(List.of(existingBase, existing1));
        when(usernameGenerator.generate("John", "Doe", Set.of("John.Doe", "John.Doe1")))
                .thenReturn("John.Doe2");

        traineeService.create(newTrainee);

        assertAll(
                () -> assertThat(newTrainee.getUsername()).isEqualTo("John.Doe2"),
                () -> assertThat(newTrainee.getPassword()).isEqualTo("Passw0rd12"),
                () -> verify(passwordGenerator).generate(),
                () -> verify(traineeDao).save(newTrainee)
        );
    }

    @Test
    void updateShouldSaveTraineeChanges() {
        Trainee trainee = Trainee.builder()
                .userId(10L)
                .firstName("Hermione")
                .lastName("Granger")
                .username("Hermione.Granger")
                .build();
        when(traineeDao.findById(10L)).thenReturn(Optional.of(trainee));

        traineeService.update(trainee);

        assertAll(
                () -> verify(traineeDao).findById(10L),
                () -> verify(traineeDao).save(trainee),
                () -> verifyNoMoreInteractions(traineeDao)
        );
    }

    @Test
    void updateShouldThrowRuntimeExceptionWhenDaoFails() {
        Trainee trainee = Trainee.builder().userId(10L).build();
        RuntimeException exception = new RuntimeException("DAO failure");
        when(traineeDao.findById(10L)).thenReturn(Optional.of(trainee));
        doThrow(exception).when(traineeDao).save(trainee);

        assertThatThrownBy(() -> traineeService.update(trainee))
                .isSameAs(exception);
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
