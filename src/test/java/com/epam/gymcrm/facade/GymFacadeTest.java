package com.epam.gymcrm.facade;

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
    void findTraineeByIdShouldReturnTraineeFromService() {
        Trainee trainee = trainee(1L, "John", "Doe", "John.Doe");
        when(traineeService.findById(1L)).thenReturn(trainee);

        Trainee result = gymFacade.findTraineeById(1L);

        assertAll(
                () -> assertThat(result).isSameAs(trainee),
                () -> verify(traineeService).findById(1L)
        );
    }

    @Test
    void updateTraineeShouldDelegateToTraineeService() {
        Trainee trainee = trainee(1L, "John", "Doe", "John.Doe");

        gymFacade.updateTrainee(trainee);

        verify(traineeService).update(trainee);
    }

    @Test
    void deleteTraineeShouldDelegateToTraineeService() {
        gymFacade.deleteTrainee(7L);

        verify(traineeService).delete(7L);
    }

    @Test
    void createTrainerShouldDelegateToTrainerService() {
        Trainer trainer = trainer("Mike", "Stone", "Mike.Stone");

        gymFacade.createTrainer(trainer);

        verify(trainerService).create(trainer);
    }

    @Test
    void findTrainerByIdShouldReturnTrainerFromService() {
        Trainer trainer = trainer(2L, "Mike", "Stone", "Mike.Stone");
        when(trainerService.findById(2L)).thenReturn(trainer);

        Trainer result = gymFacade.findTrainerById(2L);

        assertAll(
                () -> assertThat(result).isSameAs(trainer),
                () -> verify(trainerService).findById(2L)
        );
    }

    @Test
    void updateTrainerShouldDelegateToTrainerService() {
        Trainer trainer = trainer(2L, "Mike", "Stone", "Mike.Stone");

        gymFacade.updateTrainer(trainer);

        verify(trainerService).update(trainer);
    }

    @Test
    void createTrainingShouldDelegateToTrainingService() {
        Trainee trainee = trainee(1L, "John", "Doe", "John.Doe");
        Trainer trainer = trainer(2L, "Mike", "Stone", "Mike.Stone");
        Training training = training(trainee, trainer, trainingType("Yoga"));

        gymFacade.createTraining(training);

        verify(trainingService).create(training);
    }

    @Test
    void findTrainingByIdShouldReturnTrainingFromService() {
        Trainee trainee = trainee(1L, "John", "Doe", "John.Doe");
        Trainer trainer = trainer(2L, "Mike", "Stone", "Mike.Stone");
        Training training = training(trainee, trainer, trainingType("Yoga"));
        training.setTrainingId(3L);
        when(trainingService.findById(3L)).thenReturn(training);

        Training result = gymFacade.findTrainingById(3L);

        assertAll(
                () -> assertThat(result).isSameAs(training),
                () -> verify(trainingService).findById(3L)
        );
    }
}
