package com.epam.gymcrm.dto;

import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.auth.LoginRequest;
import com.epam.gymcrm.dto.auth.LoginResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class DtoPrivacyTest {

    private static final Set<Class<?>> RESPONSE_DTOS = Set.of(
            TraineeProfileResponse.class,
            TrainerProfileResponse.class,
            TrainerSummaryResponse.class,
            TraineeTrainingResponse.class,
            TrainerTrainingResponse.class
    );

    @Test
    void responseDtosShouldNotExposePasswordsOrInternalIds() {
        RESPONSE_DTOS.forEach(dtoClass -> assertThat(componentNames(dtoClass))
                .as(dtoClass.getSimpleName())
                .doesNotContain("password", "oldPassword", "newPassword", "id"));
    }

    @Test
    void trainingResponsesShouldNotExposeRawUserFields() {
        assertAll(
                () -> assertThat(componentNames(TraineeTrainingResponse.class))
                        .doesNotContain("traineeUsername", "traineeFirstName", "traineeLastName",
                                "trainerUsername", "trainerFirstName", "trainerLastName"),
                () -> assertThat(componentNames(TrainerTrainingResponse.class))
                        .doesNotContain("traineeUsername", "traineeFirstName", "traineeLastName",
                                "trainerUsername", "trainerFirstName", "trainerLastName")
        );
    }

    @Test
    void dtoToStringShouldRedactSensitiveValues() {
        List<String> representations = List.of(
                new AuthRequest("John.Doe", "secret").toString(),
                new LoginRequest("John.Doe", "secret").toString(),
                new LoginResponse("auth-token", ProfileType.TRAINEE).toString(),
                new AuthenticatedUser("John.Doe", "secret", ProfileType.TRAINEE).toString(),
                new ChangePasswordRequest("John.Doe", "old-secret", "new-secret").toString(),
                new UsernamePasswordResponse("John.Doe", "generated-secret").toString(),
                new CreateTraineeRequest(
                        "John",
                        "Doe",
                        LocalDate.of(1995, 1, 10),
                        "Main Street, 123"
                ).toString(),
                new UpdateTraineeRequest(
                        "John.Doe",
                        "secret",
                        "John",
                        "Doe",
                        LocalDate.of(1995, 1, 10),
                        "Main Street, 123"
                ).toString(),
                new UpdateTraineeTrainersRequest(
                        "John.Doe",
                        "secret",
                        List.of("Trainer.User")
                ).toString(),
                new TraineeProfileResponse(
                        "John.Doe",
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(1995, 1, 10),
                        "Main Street, 123",
                        List.of()
                ).toString(),
                new CreateTrainerRequest("Mike", "Stone", "Fitness").toString(),
                new UpdateTrainerRequest("Mike.Stone", "secret", "Mike", "Stone", "Fitness").toString(),
                new TrainerProfileResponse("Mike.Stone", "Mike", "Stone", true, "Fitness").toString(),
                new TrainerSummaryResponse("Mike.Stone", "Mike", "Stone", "Fitness").toString(),
                new AddTrainingRequest(
                        "John.Doe",
                        "secret",
                        "Mike.Stone",
                        "Yoga Basics",
                        "Yoga",
                        LocalDate.of(2026, 5, 3),
                        60
                ).toString(),
                new TraineeTrainingsRequest(
                        "John.Doe",
                        "secret",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31),
                        "Mike",
                        "Yoga",
                        PageRequest.firstPage()
                ).toString(),
                new TrainerTrainingsRequest(
                        "Mike.Stone",
                        "secret",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 28),
                        "John",
                        PageRequest.firstPage()
                ).toString(),
                new TraineeTrainingResponse(
                        "Yoga Basics",
                        "Yoga",
                        LocalDate.of(2026, 5, 3),
                        60,
                        "Mike Stone"
                ).toString(),
                new TrainerTrainingResponse(
                        "Yoga Basics",
                        "Yoga",
                        LocalDate.of(2026, 5, 3),
                        60,
                        "John Doe"
                ).toString()
        );

        representations.forEach(representation -> assertThat(representation)
                .doesNotContain(
                        "John.Doe",
                        "Mike.Stone",
                        "secret",
                        "old-secret",
                        "new-secret",
                        "generated-secret",
                        "auth-token",
                        "Main Street, 123",
                        "1995-01-10",
                        "Trainer.User",
                        "Mike Stone",
                        "John Doe"
                ));
    }

    private static List<String> componentNames(Class<?> recordClass) {
        return Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();
    }
}
