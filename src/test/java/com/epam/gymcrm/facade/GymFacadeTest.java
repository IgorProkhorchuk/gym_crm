package com.epam.gymcrm.facade;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @InjectMocks
    private GymFacade gymFacade;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Test
    void createTraineeShouldDelegateToTraineeService() {
        Trainee trainee = trainee("John", "Doe", "John.Doe");

        gymFacade.createTrainee(trainee);

        verify(traineeService).create(trainee);
    }

    @Test
    void getTraineeProfileShouldReturnTraineeFromService() {
        Trainee trainee = trainee(1L, "John", "Doe", "John.Doe");
        when(traineeService.getProfile("John.Doe", "password")).thenReturn(trainee);

        Trainee result = gymFacade.getTraineeProfile("John.Doe", "password");

        assertAll(
                () -> assertThat(result).isSameAs(trainee),
                () -> verify(traineeService).getProfile("John.Doe", "password")
        );
    }

    @Test
    void changeTraineePasswordShouldDelegateToTraineeService() {
        gymFacade.changeTraineePassword("John.Doe", "old-password", "new-password");

        verify(traineeService).changePassword("John.Doe", "old-password", "new-password");
    }

    @Test
    void activateTraineeShouldDelegateToTraineeService() {
        gymFacade.activateTrainee("John.Doe", "password");

        verify(traineeService).activate("John.Doe", "password");
    }

    @Test
    void deactivateTraineeShouldDelegateToTraineeService() {
        gymFacade.deactivateTrainee("John.Doe", "password");

        verify(traineeService).deactivate("John.Doe", "password");
    }

    @Test
    void deleteTraineeByUsernameShouldDelegateToTraineeService() {
        gymFacade.deleteTraineeByUsername("John.Doe", "password");

        verify(traineeService).deleteByUsername("John.Doe", "password");
    }

    @Test
    void updateTraineeTrainersShouldReturnTrainersFromService() {
        List<String> trainerUsernames = List.of("First.Trainer", "Second.Trainer");
        List<Trainer> trainers = List.of(
                trainer("First", "Trainer", "First.Trainer"),
                trainer("Second", "Trainer", "Second.Trainer")
        );
        when(traineeService.updateTrainers("John.Doe", "password", trainerUsernames)).thenReturn(trainers);

        List<Trainer> result = gymFacade.updateTraineeTrainers("John.Doe", "password", trainerUsernames);

        assertAll(
                () -> assertThat(result).isSameAs(trainers),
                () -> verify(traineeService).updateTrainers("John.Doe", "password", trainerUsernames)
        );
    }

    @Test
    void updateTraineeShouldDelegateToTraineeService() {
        Trainee trainee = trainee(1L, "John", "Doe", "John.Doe");

        gymFacade.updateTrainee("John.Doe", "password", trainee);

        verify(traineeService).update("John.Doe", "password", trainee);
    }

    @Test
    void createTrainerShouldDelegateToTrainerService() {
        Trainer trainer = trainer("Mike", "Stone", "Mike.Stone");

        gymFacade.createTrainer(trainer);

        verify(trainerService).create(trainer);
    }

    @Test
    void getTrainerProfileShouldReturnTrainerFromService() {
        Trainer trainer = trainer(2L, "Mike", "Stone", "Mike.Stone");
        when(trainerService.getProfile("Mike.Stone", "password")).thenReturn(trainer);

        Trainer result = gymFacade.getTrainerProfile("Mike.Stone", "password");

        assertAll(
                () -> assertThat(result).isSameAs(trainer),
                () -> verify(trainerService).getProfile("Mike.Stone", "password")
        );
    }

    @Test
    void changeTrainerPasswordShouldDelegateToTrainerService() {
        gymFacade.changeTrainerPassword("Mike.Stone", "old-password", "new-password");

        verify(trainerService).changePassword("Mike.Stone", "old-password", "new-password");
    }

    @Test
    void activateTrainerShouldDelegateToTrainerService() {
        gymFacade.activateTrainer("Mike.Stone", "password");

        verify(trainerService).activate("Mike.Stone", "password");
    }

    @Test
    void deactivateTrainerShouldDelegateToTrainerService() {
        gymFacade.deactivateTrainer("Mike.Stone", "password");

        verify(trainerService).deactivate("Mike.Stone", "password");
    }

    @Test
    void getUnassignedTrainersShouldReturnTrainersFromService() {
        List<Trainer> trainers = List.of(trainer("Available", "Trainer", "Available.Trainer"));
        when(trainerService.getUnassignedTrainers("John.Doe", "password")).thenReturn(trainers);

        List<Trainer> result = gymFacade.getUnassignedTrainers("John.Doe", "password");

        assertAll(
                () -> assertThat(result).isSameAs(trainers),
                () -> verify(trainerService).getUnassignedTrainers("John.Doe", "password")
        );
    }

    @Test
    void updateTrainerShouldDelegateToTrainerService() {
        Trainer trainer = trainer(2L, "Mike", "Stone", "Mike.Stone");

        gymFacade.updateTrainer("Mike.Stone", "password", trainer);

        verify(trainerService).update("Mike.Stone", "password", trainer);
    }

    @Test
    void addTrainingShouldDelegateToTrainingService() {
        AddTrainingRequest request = new AddTrainingRequest(
                "Mike.Stone",
                "Yoga Basics",
                "Yoga",
                LocalDate.of(2026, 5, 3),
                60
        );

        gymFacade.addTraining("John.Doe", "password", request);

        verify(trainingService).addTraining("John.Doe", "password", request);
    }

    @Test
    void getTraineeTrainingsShouldReturnTrainingsFromService() {
        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "Mike",
                "Yoga"
        );
        List<Training> trainings = List.of(training(
                trainee(1L, "John", "Doe", "John.Doe"),
                trainer(2L, "Mike", "Stone", "Mike.Stone"),
                trainingType("Yoga")
        ));
        when(trainingService.getTraineeTrainings("John.Doe", "password", criteria)).thenReturn(trainings);

        List<Training> result = gymFacade.getTraineeTrainings("John.Doe", "password", criteria);

        assertAll(
                () -> assertThat(result).isSameAs(trainings),
                () -> verify(trainingService).getTraineeTrainings("John.Doe", "password", criteria)
        );
    }

    @Test
    void getTrainerTrainingsShouldReturnTrainingsFromService() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                "John"
        );
        List<Training> trainings = List.of(training(
                trainee(1L, "John", "Doe", "John.Doe"),
                trainer(2L, "Mike", "Stone", "Mike.Stone"),
                trainingType("Yoga")
        ));
        when(trainingService.getTrainerTrainings("Mike.Stone", "password", criteria)).thenReturn(trainings);

        List<Training> result = gymFacade.getTrainerTrainings("Mike.Stone", "password", criteria);

        assertAll(
                () -> assertThat(result).isSameAs(trainings),
                () -> verify(trainingService).getTrainerTrainings("Mike.Stone", "password", criteria)
        );
    }
}
