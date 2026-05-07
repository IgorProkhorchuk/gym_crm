package com.epam.gymcrm.mapper;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainingResponse;
import com.epam.gymcrm.model.Training;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        builder = @Builder(disableBuilder = true)
)
public interface TrainingMapper {

    @Mapping(target = "trainingId", ignore = true)
    @Mapping(target = "trainee", ignore = true)
    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "trainingType", ignore = true)
    @Mapping(target = "trainingName", source = "trainingName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    Training toEntity(AddTrainingRequest request);

    @Mapping(target = "id", source = "trainingId")
    @Mapping(target = "trainingName", source = "trainingName")
    @Mapping(target = "trainingType", source = "trainingType.trainingTypeName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    @Mapping(target = "traineeUsername", source = "trainee.user.username")
    @Mapping(target = "traineeFirstName", source = "trainee.user.firstName")
    @Mapping(target = "traineeLastName", source = "trainee.user.lastName")
    @Mapping(target = "trainerUsername", source = "trainer.user.username")
    @Mapping(target = "trainerFirstName", source = "trainer.user.firstName")
    @Mapping(target = "trainerLastName", source = "trainer.user.lastName")
    TrainingResponse toResponse(Training training);

    TraineeTrainingCriteria toCriteria(TraineeTrainingsRequest request);

    TrainerTrainingCriteria toCriteria(TrainerTrainingsRequest request);
}
