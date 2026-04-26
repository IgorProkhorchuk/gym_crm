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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

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
    void testCreateTrainee() {
        Trainee trainee = Trainee.builder().firstName("John").build();
        gymFacade.createTrainee(trainee);
        verify(traineeService).create(trainee);
    }

    @Test
    void testFindTraineeById() {
        Trainee trainee = Trainee.builder().userId(1L).firstName("John").build();
        when(traineeService.findById(1L)).thenReturn(trainee);

        Trainee result = gymFacade.findTraineeById(1L);

        assertAll(
                () -> assertThat(result).isSameAs(trainee),
                () -> verify(traineeService).findById(1L)
        );
    }

    @Test
    void testUpdateTrainee() {
        Trainee trainee = Trainee.builder().userId(1L).firstName("John").build();
        gymFacade.updateTrainee(trainee);
        verify(traineeService).update(trainee);
    }

    @Test
    void testDeleteTrainee() {
        gymFacade.deleteTrainee(7L);
        verify(traineeService).delete(7L);
    }

    @Test
    void testCreateTrainer() {
        Trainer trainer = Trainer.builder().firstName("Mike").build();
        gymFacade.createTrainer(trainer);
        verify(trainerService).create(trainer);
    }

    @Test
    void testFindTrainerById() {
        Trainer trainer = Trainer.builder().userId(2L).firstName("Mike").build();
        when(trainerService.findById(2L)).thenReturn(trainer);

        Trainer result = gymFacade.findTrainerById(2L);

        assertAll(
                () -> assertThat(result).isSameAs(trainer),
                () -> verify(trainerService).findById(2L)
        );
    }

    @Test
    void testUpdateTrainer() {
        Trainer trainer = Trainer.builder().userId(2L).firstName("Mike").build();
        gymFacade.updateTrainer(trainer);
        verify(trainerService).update(trainer);
    }

    @Test
    void testCreateTraining() {
        Training training = Training.builder().trainingName("Yoga").build();
        gymFacade.createTraining(training);
        verify(trainingService).create(training);
    }

    @Test
    void testFindTrainingById() {
        Training training = Training.builder().trainingId(3L).trainingName("Yoga").build();
        when(trainingService.findById(3L)).thenReturn(training);

        Training result = gymFacade.findTrainingById(3L);

        assertAll(
                () -> assertThat(result).isSameAs(training),
                () -> verify(trainingService).findById(3L)
        );
    }
}
