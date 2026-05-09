package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import org.junit.jupiter.api.Test;

import static com.epam.gymcrm.TestFixtures.trainer;
import static org.assertj.core.api.Assertions.assertThat;

class TrainerMapperTest {

    private final TrainerMapperImpl trainerMapper = new TrainerMapperImpl();

    @Test
    void toEntityShouldMapCreateRequestToTrainer() {
        CreateTrainerRequest request = new CreateTrainerRequest("John", "Smith", "Fitness");

        Trainer trainer = trainerMapper.toEntity(request);

        assertThat(trainer.getId()).isNull();
        assertThat(trainer.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Smith");
        assertThat(trainer.getUser().getActive()).isTrue();
        assertThat(trainer.getUser().getUsername()).isNull();
        assertThat(trainer.getUser().getPassword()).isNull();
        assertThat(trainer.getSpecialization().getTrainingTypeId()).isNull();
        assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Fitness");
    }

    @Test
    void toEntityShouldReturnNullWhenRequestIsNull() {
        assertThat(trainerMapper.toEntity(null)).isNull();
        assertThat(trainerMapper.createTrainerRequestToUser(null)).isNull();
        assertThat(trainerMapper.createTrainerRequestToTrainingType(null)).isNull();
    }

    @Test
    void toProfileResponseShouldMapTrainer() {
        Trainer trainer = trainer("John", "Smith", "John.Smith");

        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        assertThat(response).isEqualTo(new TrainerProfileResponse(
                "John.Smith",
                "John",
                "Smith",
                true,
                "Fitness"
        ));
    }

    @Test
    void toSummaryResponseShouldMapTrainer() {
        Trainer trainer = trainer("John", "Smith", "John.Smith");

        TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

        assertThat(response).isEqualTo(new TrainerSummaryResponse(
                "John.Smith",
                "John",
                "Smith",
                "Fitness"
        ));
    }

    @Test
    void responsesShouldHandleNullSourceAndNestedValues() {
        Trainer trainer = new Trainer();

        TrainerProfileResponse profile = trainerMapper.toProfileResponse(trainer);
        TrainerSummaryResponse summary = trainerMapper.toSummaryResponse(trainer);

        assertThat(trainerMapper.toProfileResponse(null)).isNull();
        assertThat(trainerMapper.toSummaryResponse(null)).isNull();
        assertThat(profile.username()).isNull();
        assertThat(profile.firstName()).isNull();
        assertThat(profile.lastName()).isNull();
        assertThat(profile.active()).isNull();
        assertThat(profile.specialization()).isNull();
        assertThat(summary.username()).isNull();
        assertThat(summary.firstName()).isNull();
        assertThat(summary.lastName()).isNull();
        assertThat(summary.specialization()).isNull();
    }

    @Test
    void updateFromRequestShouldUpdateAllowedTrainerFieldsOnly() {
        Trainer trainer = trainer("John", "Smith", "John.Smith");
        trainer.getUser().setUserId(22L);
        trainer.getUser().setPassword("old-password");
        trainer.getUser().setActive(false);
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                "Ignored.Username",
                "ignored-password",
                "Johnny",
                "Done",
                "Yoga"
        );

        trainerMapper.updateFromRequest(request, trainer);

        assertThat(trainer.getUser().getUserId()).isEqualTo(22L);
        assertThat(trainer.getUser().getUsername()).isEqualTo("John.Smith");
        assertThat(trainer.getUser().getPassword()).isEqualTo("old-password");
        assertThat(trainer.getUser().getActive()).isFalse();
        assertThat(trainer.getUser().getFirstName()).isEqualTo("Johnny");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Done");
        assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Fitness");
    }

    @Test
    void updateFromRequestShouldCreateUserWhenMissingAndIgnoreNullRequest() {
        Trainer trainer = new Trainer();
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                "Ignored.Username",
                "ignored-password",
                "John",
                "Smith",
                "Fitness"
        );

        trainerMapper.updateFromRequest(null, trainer);
        assertThat(trainer.getUser()).isNull();

        trainerMapper.updateFromRequest(request, trainer);
        assertThat(trainer.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Smith");

        User user = new User();
        trainerMapper.updateTrainerRequestToUser(null, user);
        assertThat(user.getFirstName()).isNull();
    }
}
