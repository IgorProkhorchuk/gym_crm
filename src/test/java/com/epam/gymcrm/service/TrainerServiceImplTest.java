package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.exception.EntityNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Test
    void testCreateTrainerGeneratesUsernameAndPassword() {
        Trainer trainer = Trainer.builder().firstName("Severus").lastName("Snape").build();
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());
        when(usernameGenerator.generate("Severus", "Snape", Collections.emptySet())).thenReturn("Severus.Snape");

        trainerService.create(trainer);

        assertAll(
                () -> assertThat(trainer.getUsername()).isEqualTo("Severus.Snape"),
                () -> assertThat(trainer.getPassword()).isEqualTo("Passw0rd12"),
                () -> verify(usernameGenerator).generate("Severus", "Snape", Collections.emptySet()),
                () -> verify(passwordGenerator).generate(),
                () -> verify(trainerDao).save(trainer)
        );
    }

    @Test
    void testCreateTrainerWithExistingUsernameGeneratesSuffix() {
        Trainer newTrainer = Trainer.builder().firstName("Severus").lastName("Snape").build();
        Trainer existingTrainer = Trainer.builder().username("Severus.Snape").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));
        when(usernameGenerator.generate("Severus", "Snape", Set.of("Severus.Snape")))
                .thenReturn("Severus.Snape1");

        trainerService.create(newTrainer);

        assertAll(
                () -> assertThat(newTrainer.getUsername()).isEqualTo("Severus.Snape1"),
                () -> verify(usernameGenerator).generate("Severus", "Snape", Set.of("Severus.Snape"))
        );
    }

    @Test
    void testCreateTrainerPassesExistingUsernamesToGenerator() {
        Trainer newTrainer = Trainer.builder().firstName("Severus").lastName("Snape").build();

        Trainer existingBase = Trainer.builder().username("Severus.Snape").build();
        Trainer existing2 = Trainer.builder().username("Severus.Snape2").build();
        Trainer similarName = Trainer.builder().username("Severus.Snapely").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(List.of(existingBase, existing2, similarName));
        when(usernameGenerator.generate(
                "Severus",
                "Snape",
                Set.of("Severus.Snape", "Severus.Snape2", "Severus.Snapely")
        )).thenReturn("Severus.Snape1");

        trainerService.create(newTrainer);

        assertAll(
                () -> assertThat(newTrainer.getUsername()).isEqualTo("Severus.Snape1"),
                () -> verify(usernameGenerator).generate(
                        "Severus",
                        "Snape",
                        Set.of("Severus.Snape", "Severus.Snape2", "Severus.Snapely")
                )
        );
    }

    @Test
    void testFindByIdReturnsTrainer() {
        Trainer trainer = Trainer.builder().userId(50L).firstName("Minerva").build();
        when(trainerDao.findById(50L)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.findById(50L);

        assertAll(
                () -> assertThat(result).isSameAs(trainer),
                () -> assertThat(result.getFirstName()).isEqualTo("Minerva"),
                () -> verify(trainerDao).findById(50L)
        );
    }

    @Test
    void testFindByIdThrowsWhenNotFound() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainer profile not found");

        verify(trainerDao).findById(99L);
    }

    @Test
    void testCreateTrainerRethrowsDaoFailure() {
        Trainer trainer = Trainer.builder().userId(30L).firstName("Severus").lastName("Snape").build();
        RuntimeException exception = new RuntimeException("DAO failure");

        when(trainerDao.findAll()).thenReturn(Collections.emptyList());
        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(usernameGenerator.generate("Severus", "Snape", Collections.emptySet()))
                .thenReturn("Severus.Snape");
        doThrow(exception).when(trainerDao).save(trainer);

        assertThatThrownBy(() -> trainerService.create(trainer))
                .isSameAs(exception);
    }

    @Test
    void testCreateTrainerRejectsNullTrainer() {
        assertThatThrownBy(() -> trainerService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer must not be null");
    }

    @Test
    void testCreateTrainerSkipsTakenSequentialSuffixes() {
        Trainer newTrainer = Trainer.builder()
                .firstName("Severus")
                .lastName("Snape")
                .build();

        Trainer existingBase = Trainer.builder().username("Severus.Snape").build();
        Trainer existing1 = Trainer.builder().username("Severus.Snape1").build();

        when(passwordGenerator.generate()).thenReturn("Passw0rd12");
        when(trainerDao.findAll()).thenReturn(List.of(existingBase, existing1));
        when(usernameGenerator.generate("Severus", "Snape", Set.of("Severus.Snape", "Severus.Snape1")))
                .thenReturn("Severus.Snape2");

        trainerService.create(newTrainer);

        assertAll(
                () -> assertThat(newTrainer.getUsername()).isEqualTo("Severus.Snape2"),
                () -> assertThat(newTrainer.getPassword()).isEqualTo("Passw0rd12"),
                () -> verify(passwordGenerator).generate(),
                () -> verify(trainerDao).save(newTrainer)
        );
    }

    @Test
    void testUpdateTrainerDelegatesToDao() {
        Trainer trainer = Trainer.builder()
                .userId(22L)
                .firstName("Minerva")
                .lastName("McGonagall")
                .username("Minerva.McGonagall")
                .build();

        trainerService.update(trainer);

        assertAll(
                () -> verify(trainerDao).save(trainer),
                () -> verifyNoMoreInteractions(trainerDao)
        );
    }

    @Test
    void testUpdateTrainerRethrowsDaoFailure() {
        Trainer trainer = Trainer.builder().userId(22L).build();
        RuntimeException exception = new RuntimeException("DAO failure");
        doThrow(exception).when(trainerDao).save(trainer);

        assertThatThrownBy(() -> trainerService.update(trainer))
                .isSameAs(exception);
    }

    @Test
    void testFindTrainerByIdRethrowsDaoFailure() {
        RuntimeException exception = new RuntimeException("DAO failure");
        when(trainerDao.findById(22L)).thenThrow(exception);

        assertThatThrownBy(() -> trainerService.findById(22L))
                .isSameAs(exception);
    }

    @Test
    void testFindTrainerByIdRejectsNullId() {
        assertThatThrownBy(() -> trainerService.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer id must not be null");
    }

}
