package com.epam.gymcrm.client.workload;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.epam.gymcrm.model.Training;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TrainerWorkloadRequestFactoryTest {

  private final TrainerWorkloadRequestFactory requestFactory = new TrainerWorkloadRequestFactory();

  @Test
  void fromTrainingShouldCreateTrainerWorkloadRequest() {
    Training training = training(
        trainee("John", "Doe", "John.Doe"),
        trainer("Coach", "Stone", "Coach.Stone"),
        trainingType("Yoga"));

    TrainerWorkloadRequest result =
        requestFactory.fromTraining(training, TrainerWorkloadActionType.ADD);

    assertAll(
        () -> assertThat(result.trainerUsername()).isEqualTo("Coach.Stone"),
        () -> assertThat(result.trainerFirstName()).isEqualTo("Coach"),
        () -> assertThat(result.trainerLastName()).isEqualTo("Stone"),
        () -> assertThat(result.trainerStatus()).isTrue(),
        () -> assertThat(result.trainingDate()).isEqualTo(LocalDate.of(2026, 5, 3)),
        () -> assertThat(result.trainingDuration()).isEqualTo(60),
        () -> assertThat(result.actionType()).isEqualTo(TrainerWorkloadActionType.ADD));
  }
}
