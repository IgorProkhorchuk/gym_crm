package com.epam.gymcrm.mapper;

import static com.epam.gymcrm.TestFixtures.trainee;
import static com.epam.gymcrm.TestFixtures.trainer;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TraineeMapperTest {

  private final TraineeMapperImpl traineeMapper = traineeMapper();

  @Test
  void toEntityShouldMapCreateRequestToTrainee() {
    CreateTraineeRequest request =
        new CreateTraineeRequest("Jane", "Doe", LocalDate.of(1995, 1, 10), "Main Street");

    Trainee trainee = traineeMapper.toEntity(request);

    assertThat(trainee.getId()).isNull();
    assertThat(trainee.getUser().getFirstName()).isEqualTo("Jane");
    assertThat(trainee.getUser().getLastName()).isEqualTo("Doe");
    assertThat(trainee.getUser().getActive()).isTrue();
    assertThat(trainee.getUser().getUsername()).isNull();
    assertThat(trainee.getUser().getPassword()).isNull();
    assertThat(trainee.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 1, 10));
    assertThat(trainee.getAddress()).isEqualTo("Main Street");
  }

  @Test
  void toEntityShouldReturnNullWhenRequestIsNull() {
    assertThat(traineeMapper.toEntity(null)).isNull();
    assertThat(traineeMapper.createTraineeRequestToUser(null)).isNull();
  }

  @Test
  void toProfileResponseShouldMapTraineeAndAssignedTrainers() {
    Trainer trainer = trainer("John", "Smith", "John.Smith");
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    trainee.setTrainers(new LinkedHashSet<>(Set.of(trainer)));

    TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

    assertThat(response.username()).isEqualTo("Jane.Doe");
    assertThat(response.firstName()).isEqualTo("Jane");
    assertThat(response.lastName()).isEqualTo("Doe");
    assertThat(response.active()).isTrue();
    assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1995, 1, 10));
    assertThat(response.address()).isEqualTo("Main Street, 123");
    assertThat(response.trainers())
        .containsExactly(new TrainerSummaryResponse("John.Smith", "John", "Smith", "Fitness"));
  }

  @Test
  void toProfileResponseShouldHandleNullSourceAndNestedValues() {
    Trainee trainee = new Trainee();
    trainee.setTrainers(null);

    TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

    assertThat(traineeMapper.toProfileResponse(null)).isNull();
    assertThat(response.username()).isNull();
    assertThat(response.firstName()).isNull();
    assertThat(response.lastName()).isNull();
    assertThat(response.active()).isNull();
    assertThat(response.trainers()).isNull();
    assertThat(traineeMapper.trainerSetToTrainerSummaryResponseList(null)).isNull();
  }

  @Test
  void updateFromRequestShouldUpdateAllowedTraineeFieldsOnly() {
    Trainee trainee = trainee("Jane", "Doe", "Jane.Doe");
    trainee.getUser().setUserId(12L);
    trainee.getUser().setPassword("old-password");
    trainee.getUser().setActive(false);
    UpdateTraineeRequest request =
        new UpdateTraineeRequest(
            "Ignored.Username",
            "ignored-password",
            "Janet",
            "Done",
            LocalDate.of(1996, 2, 11),
            "Updated Street",
            true);

    traineeMapper.updateFromRequest(request, trainee);

    assertThat(trainee.getUser().getUserId()).isEqualTo(12L);
    assertThat(trainee.getUser().getUsername()).isEqualTo("Jane.Doe");
    assertThat(trainee.getUser().getPassword()).isEqualTo("old-password");
    assertThat(trainee.getUser().getActive()).isTrue();
    assertThat(trainee.getUser().getFirstName()).isEqualTo("Janet");
    assertThat(trainee.getUser().getLastName()).isEqualTo("Done");
    assertThat(trainee.getDateOfBirth()).isEqualTo(LocalDate.of(1996, 2, 11));
    assertThat(trainee.getAddress()).isEqualTo("Updated Street");
  }

  @Test
  void updateFromRequestShouldCreateUserWhenMissingAndIgnoreNullRequest() {
    Trainee trainee = new Trainee();
    UpdateTraineeRequest request =
        new UpdateTraineeRequest(
            "Ignored.Username",
            "ignored-password",
            "Jane",
            "Doe",
            LocalDate.of(1995, 1, 10),
            "Main Street",
            true);

    traineeMapper.updateFromRequest(null, trainee);
    assertThat(trainee.getUser()).isNull();

    traineeMapper.updateFromRequest(request, trainee);
    assertThat(trainee.getUser().getFirstName()).isEqualTo("Jane");
    assertThat(trainee.getUser().getLastName()).isEqualTo("Doe");
    assertThat(trainee.getUser().getActive()).isTrue();

    User user = new User();
    traineeMapper.updateTraineeRequestToUser(null, user);
    assertThat(user.getFirstName()).isNull();
  }

  private static TraineeMapperImpl traineeMapper() {
    TraineeMapperImpl mapper = new TraineeMapperImpl();
    ReflectionTestUtils.setField(mapper, "trainerMapper", new TrainerMapperImpl());
    return mapper;
  }
}
