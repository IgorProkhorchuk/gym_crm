package com.epam.gymcrm.mapper;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static com.epam.gymcrm.TestFixtures.training;
import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.model.Training;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TrainingMapperTest {

  private final TrainingMapperImpl trainingMapper = new TrainingMapperImpl();

  @Test
  void fullNameShouldJoinFirstAndLastName() {
    assertThat(trainingMapper.fullName("Jane", "Doe")).isEqualTo("Jane Doe");
  }

  @Test
  void toEntityShouldMapAddTrainingRequest() {
    AddTrainingRequest request =
        new AddTrainingRequest(
            "Jane.Doe",
            "John.Smith",
            "Yoga Basics",
            "Yoga",
            LocalDate.of(2026, 5, 3),
            60);

    Training training = trainingMapper.toEntity(request);

    assertThat(training.getTrainingId()).isNull();
    assertThat(training.getTrainee()).isNull();
    assertThat(training.getTrainer()).isNull();
    assertThat(training.getTrainingType()).isNull();
    assertThat(training.getTrainingName()).isEqualTo("Yoga Basics");
    assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2026, 5, 3));
    assertThat(training.getTrainingDuration()).isEqualTo(60);
  }

  @Test
  void toEntityShouldReturnNullWhenRequestIsNull() {
    assertThat(trainingMapper.toEntity(null)).isNull();
  }

  @Test
  void toTraineeTrainingResponseShouldMapTraining() {
    Training training =
        training(
            trainee("Jane", "Doe", "Jane.Doe"),
            trainer("John", "Smith", "John.Smith"),
            trainingType("Yoga"));

    TraineeTrainingResponse response = trainingMapper.toTraineeTrainingResponse(training);

    assertThat(response)
        .isEqualTo(
            new TraineeTrainingResponse(
                "Yoga Basics", "Yoga", LocalDate.of(2026, 5, 3), 60, "John Smith"));
  }

  @Test
  void toTrainerTrainingResponseShouldMapTraining() {
    Training training =
        training(
            trainee("Jane", "Doe", "Jane.Doe"),
            trainer("John", "Smith", "John.Smith"),
            trainingType("Yoga"));

    TrainerTrainingResponse response = trainingMapper.toTrainerTrainingResponse(training);

    assertThat(response)
        .isEqualTo(
            new TrainerTrainingResponse(
                "Yoga Basics", "Yoga", LocalDate.of(2026, 5, 3), 60, "Jane Doe"));
  }

  @Test
  void trainingResponsesShouldHandleNullSourceAndNullTrainingType() {
    Training training =
        training(trainee("Jane", "Doe", "Jane.Doe"), trainer("John", "Smith", "John.Smith"), null);

    assertThat(trainingMapper.toTraineeTrainingResponse(null)).isNull();
    assertThat(trainingMapper.toTrainerTrainingResponse(null)).isNull();
    assertThat(trainingMapper.toTraineeTrainingResponse(training).trainingType()).isNull();
    assertThat(trainingMapper.toTrainerTrainingResponse(training).trainingType()).isNull();
  }

  @Test
  void toCriteriaShouldMapTraineeTrainingsRequest() {
    TraineeTrainingsRequest request =
        new TraineeTrainingsRequest(
            "Jane.Doe",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            "John",
            "Yoga",
            PageRequest.firstPage());

    TraineeTrainingCriteria criteria = trainingMapper.toCriteria(request);

    assertThat(criteria)
        .isEqualTo(
            new TraineeTrainingCriteria(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "John", "Yoga"));
  }

  @Test
  void toCriteriaShouldMapTrainerTrainingsRequest() {
    TrainerTrainingsRequest request =
        new TrainerTrainingsRequest(
            "John.Smith",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            "Jane",
            PageRequest.firstPage());

    TrainerTrainingCriteria criteria = trainingMapper.toCriteria(request);

    assertThat(criteria)
        .isEqualTo(
            new TrainerTrainingCriteria(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "Jane"));
  }

  @Test
  void toCriteriaShouldReturnNullWhenRequestIsNull() {
    TraineeTrainingsRequest traineeRequest = null;
    TrainerTrainingsRequest trainerRequest = null;

    assertThat(trainingMapper.toCriteria(traineeRequest)).isNull();
    assertThat(trainingMapper.toCriteria(trainerRequest)).isNull();
  }
}
