package com.epam.gymcrm.mapper;

import com.epam.gymcrm.criteria.TraineeTrainingCriteria;
import com.epam.gymcrm.criteria.TrainerTrainingCriteria;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.model.Training;
import org.mapstruct.BeanMapping;
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

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "trainingId", ignore = true)
    @Mapping(target = "trainee", ignore = true)
    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "trainingType", ignore = true)
    @Mapping(target = "trainingName", source = "trainingName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    Training toEntity(AddTrainingRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "trainingName", source = "trainingName")
    @Mapping(target = "trainingType", source = "trainingType.trainingTypeName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    @Mapping(target = "trainerName", expression = "java(fullName(training.getTrainer().getUser().getFirstName(), "
            + "training.getTrainer().getUser().getLastName()))")
    TraineeTrainingResponse toTraineeTrainingResponse(Training training);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "trainingName", source = "trainingName")
    @Mapping(target = "trainingType", source = "trainingType.trainingTypeName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    @Mapping(target = "traineeName", expression = "java(fullName(training.getTrainee().getUser().getFirstName(), "
            + "training.getTrainee().getUser().getLastName()))")
    TrainerTrainingResponse toTrainerTrainingResponse(Training training);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fromDate", source = "fromDate")
    @Mapping(target = "toDate", source = "toDate")
    @Mapping(target = "trainerName", source = "trainerName")
    @Mapping(target = "trainingType", source = "trainingType")
    TraineeTrainingCriteria toCriteria(TraineeTrainingsRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fromDate", source = "fromDate")
    @Mapping(target = "toDate", source = "toDate")
    @Mapping(target = "traineeName", source = "traineeName")
    TrainerTrainingCriteria toCriteria(TrainerTrainingsRequest request);

    default String fullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
