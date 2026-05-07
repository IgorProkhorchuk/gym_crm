package com.epam.gymcrm.service;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private AuthenticationService authenticationService;

    private AddTrainingRequest addTrainingRequest;

    @BeforeEach
    void setUp() {
        addTrainingRequest = new AddTrainingRequest(
                "Training.Trainer",
                "Yoga Basics",
                "Yoga",
                LocalDate.of(2026, 5, 3),
                60
        );
    }

    @Test
    void addTrainingShouldAuthenticateTraineeAndSaveTraining() {
        Trainee trainee = trainee("Training", "Trainee", "Training.Trainee");
        Trainer trainer = trainer("Training", "Trainer", "Training.Trainer");
        TrainingType trainingType = trainingType("Yoga");
        when(authenticationService.authenticateTrainee("Training.Trainee", "password")).thenReturn(trainee);
        when(trainerDao.findByUsername("Training.Trainer")).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(trainingType));
        ArgumentCaptor<Training> trainingCaptor = ArgumentCaptor.forClass(Training.class);

        trainingService.addTraining("Training.Trainee", "password", addTrainingRequest);

        verify(trainingDao).save(trainingCaptor.capture());
        Training savedTraining = trainingCaptor.getValue();
        assertAll(
                () -> verify(authenticationService).authenticateTrainee("Training.Trainee", "password"),
                () -> verify(trainerDao).findByUsername("Training.Trainer"),
                () -> verify(trainingTypeDao).findByName("Yoga"),
                () -> assertThat(savedTraining.getTrainee()).isSameAs(trainee),
                () -> assertThat(savedTraining.getTrainer()).isSameAs(trainer),
                () -> assertThat(savedTraining.getTrainingType()).isSameAs(trainingType),
                () -> assertThat(savedTraining.getTrainingName()).isEqualTo("Yoga Basics"),
                () -> assertThat(savedTraining.getTrainingDate()).isEqualTo(LocalDate.of(2026, 5, 3)),
                () -> assertThat(savedTraining.getTrainingDuration()).isEqualTo(60)
        );
    }

    @Test
    void addTrainingShouldThrowEntityNotFoundExceptionWhenTrainerDoesNotExist() {
        Trainee trainee = trainee("Training", "Trainee", "Training.Trainee");
        when(authenticationService.authenticateTrainee("Training.Trainee", "password")).thenReturn(trainee);
        when(trainerDao.findByUsername("Training.Trainer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", addTrainingRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainer profile not found");
    }

    @Test
    void addTrainingShouldThrowEntityNotFoundExceptionWhenTrainingTypeDoesNotExist() {
        Trainee trainee = trainee("Training", "Trainee", "Training.Trainee");
        Trainer trainer = trainer("Training", "Trainer", "Training.Trainer");
        when(authenticationService.authenticateTrainee("Training.Trainee", "password")).thenReturn(trainee);
        when(trainerDao.findByUsername("Training.Trainer")).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", addTrainingRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Training type not found");
    }

    @Test
    void addTrainingShouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training request must not be null");

        verifyNoInteractions(authenticationService, trainerDao, trainingTypeDao, trainingDao);
    }

    @Test
    void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainerUsernameIsBlank() {
        AddTrainingRequest request = new AddTrainingRequest(
                " ",
                "Yoga Basics",
                "Yoga",
                LocalDate.of(2026, 5, 3),
                60
        );

        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer username must not be blank");
    }

    @Test
    void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainerUsernameIsNull() {
        AddTrainingRequest request = new AddTrainingRequest(
                null,
                "Yoga Basics",
                "Yoga",
                LocalDate.of(2026, 5, 3),
                60
        );

        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer username must not be blank");
    }

    @Test
    void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingNameIsBlank() {
        AddTrainingRequest request = new AddTrainingRequest(
                "Training.Trainer",
                " ",
                "Yoga",
                LocalDate.of(2026, 5, 3),
                60
        );

        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training name must not be blank");
    }

    @Test
    void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingTypeIsBlank() {
        AddTrainingRequest request = new AddTrainingRequest(
                "Training.Trainer",
                "Yoga Basics",
                " ",
                LocalDate.of(2026, 5, 3),
                60
        );

        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training type must not be blank");
    }

    @Test
    void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingDateIsNull() {
        AddTrainingRequest request = new AddTrainingRequest(
                "Training.Trainer",
                "Yoga Basics",
                "Yoga",
                null,
                60
        );

        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training date must not be null");
    }

    @Test
    void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingDurationIsNull() {
        AddTrainingRequest request = new AddTrainingRequest(
                "Training.Trainer",
                "Yoga Basics",
                "Yoga",
                LocalDate.of(2026, 5, 3),
                null
        );

        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training duration must be positive");
    }

    @Test
    void addTrainingShouldThrowIllegalArgumentExceptionWhenTrainingDurationIsZero() {
        AddTrainingRequest request = new AddTrainingRequest(
                "Training.Trainer",
                "Yoga Basics",
                "Yoga",
                LocalDate.of(2026, 5, 3),
                0
        );

        assertThatThrownBy(() -> trainingService.addTraining("Training.Trainee", "password", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Training duration must be positive");
    }

    @Test
    void getTraineeTrainingsShouldAuthenticateTraineeAndReturnMatchingTrainings() {
        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "Coach",
                "Yoga"
        );
        List<Training> trainings = List.of(validTraining());
        when(trainingDao.findByTraineeUsernameAndCriteria("Training.Trainee", criteria)).thenReturn(trainings);

        List<Training> result = trainingService.getTraineeTrainings("Training.Trainee", "password", criteria);

        assertAll(
                () -> assertThat(result).isSameAs(trainings),
                () -> verify(authenticationService).authenticateTrainee("Training.Trainee", "password"),
                () -> verify(trainingDao).findByTraineeUsernameAndCriteria("Training.Trainee", criteria)
        );
    }

    @Test
    void getTraineeTrainingsShouldUseEmptyCriteriaWhenCriteriaIsNull() {
        List<Training> trainings = List.of(validTraining());
        when(trainingDao.findByTraineeUsernameAndCriteria("Training.Trainee", TraineeTrainingCriteria.empty()))
                .thenReturn(trainings);

        List<Training> result = trainingService.getTraineeTrainings("Training.Trainee", "password", null);

        assertAll(
                () -> assertThat(result).isSameAs(trainings),
                () -> verify(authenticationService).authenticateTrainee("Training.Trainee", "password"),
                () -> verify(trainingDao).findByTraineeUsernameAndCriteria(
                        "Training.Trainee",
                        TraineeTrainingCriteria.empty()
                )
        );
    }

    @Test
    void getTrainerTrainingsShouldAuthenticateTrainerAndReturnMatchingTrainings() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                "Trainee"
        );
        List<Training> trainings = List.of(validTraining());
        when(trainingDao.findByTrainerUsernameAndCriteria("Training.Trainer", criteria)).thenReturn(trainings);

        List<Training> result = trainingService.getTrainerTrainings("Training.Trainer", "password", criteria);

        assertAll(
                () -> assertThat(result).isSameAs(trainings),
                () -> verify(authenticationService).authenticateTrainer("Training.Trainer", "password"),
                () -> verify(trainingDao).findByTrainerUsernameAndCriteria("Training.Trainer", criteria)
        );
    }

    @Test
    void getTrainerTrainingsShouldUseEmptyCriteriaWhenCriteriaIsNull() {
        List<Training> trainings = List.of(validTraining());
        when(trainingDao.findByTrainerUsernameAndCriteria("Training.Trainer", TrainerTrainingCriteria.empty()))
                .thenReturn(trainings);

        List<Training> result = trainingService.getTrainerTrainings("Training.Trainer", "password", null);

        assertAll(
                () -> assertThat(result).isSameAs(trainings),
                () -> verify(authenticationService).authenticateTrainer("Training.Trainer", "password"),
                () -> verify(trainingDao).findByTrainerUsernameAndCriteria(
                        "Training.Trainer",
                        TrainerTrainingCriteria.empty()
                )
        );
    }

    private static Training validTraining() {
        return training(
                trainee("Training", "Trainee", "Training.Trainee"),
                trainer("Training", "Trainer", "Training.Trainer"),
                trainingType("Yoga")
        );
    }
}
