package com.epam.gymcrm.facade;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

public class GymFacadeTest {

    @InjectMocks
    private GymFacade gymFacade;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTrainee() {
        Trainee trainee = Trainee.builder().firstName("John").build();
        gymFacade.createTrainee(trainee);
        verify(traineeService, times(1)).create(trainee);
    }

    @Test
    void testFindTraineeById() {
        Trainee trainee = Trainee.builder().userId(1L).firstName("John").build();
        when(traineeService.findById(1L)).thenReturn(trainee);

        Trainee result = gymFacade.findTraineeById(1L);

        assertAll(
                () -> assertSame(trainee, result),
                () -> verify(traineeService, times(1)).findById(1L)
        );
    }

    @Test
    void testUpdateTrainee() {
        Trainee trainee = Trainee.builder().userId(1L).firstName("John").build();
        gymFacade.updateTrainee(trainee);
        verify(traineeService, times(1)).update(trainee);
    }

    @Test
    void testDeleteTrainee() {
        gymFacade.deleteTrainee(7L);
        verify(traineeService, times(1)).delete(7L);
    }

    @Test
    void testCreateTrainer() {
        Trainer trainer = Trainer.builder().firstName("Mike").build();
        gymFacade.createTrainer(trainer);
        verify(trainerService, times(1)).create(trainer);
    }

    @Test
    void testFindTrainerById() {
        Trainer trainer = Trainer.builder().userId(2L).firstName("Mike").build();
        when(trainerService.findById(2L)).thenReturn(trainer);

        Trainer result = gymFacade.findTrainerById(2L);

        assertAll(
                () -> assertSame(trainer, result),
                () -> verify(trainerService, times(1)).findById(2L)
        );
    }

    @Test
    void testUpdateTrainer() {
        Trainer trainer = Trainer.builder().userId(2L).firstName("Mike").build();
        gymFacade.updateTrainer(trainer);
        verify(trainerService, times(1)).update(trainer);
    }

    @Test
    void testCreateTraining() {
        Training training = Training.builder().trainingName("Yoga").build();
        gymFacade.createTraining(training);
        verify(trainingService, times(1)).create(training);
    }

    @Test
    void testFindTrainingById() {
        Training training = Training.builder().trainingId(3L).trainingName("Yoga").build();
        when(trainingService.findById(3L)).thenReturn(training);

        Training result = gymFacade.findTrainingById(3L);

        assertAll(
                () -> assertSame(training, result),
                () -> verify(trainingService, times(1)).findById(3L)
        );
    }
}
