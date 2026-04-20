package com.epam.gymcrm.facade;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.service.TraineeService;
import com.epam.gymcrm.service.TrainerService;
import com.epam.gymcrm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class GymFacadeTest {

    private GymFacade gymFacade;
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        // Мокаємо наші нові інтерфейси
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        trainingService = mock(TrainingService.class);

        gymFacade = new GymFacade();
        gymFacade.setTraineeService(traineeService);
        gymFacade.setTrainerService(trainerService);
        gymFacade.setTrainingService(trainingService);
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
        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = gymFacade.findTraineeById(1L);
        assertTrue(result.isPresent());
        verify(traineeService, times(1)).findById(1L);
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
        when(trainerService.findById(2L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = gymFacade.findTrainerById(2L);
        assertTrue(result.isPresent());
        verify(trainerService, times(1)).findById(2L);
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
        when(trainingService.findById(3L)).thenReturn(Optional.of(training));

        Optional<Training> result = gymFacade.findTrainingById(3L);
        assertTrue(result.isPresent());
        verify(trainingService, times(1)).findById(3L);
    }
}