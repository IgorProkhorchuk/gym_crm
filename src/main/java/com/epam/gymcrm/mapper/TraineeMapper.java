package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.model.Trainee;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = TrainerMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        builder = @Builder(disableBuilder = true)
)
public interface TraineeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.userId", ignore = true)
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.active", source = "active")
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toEntity(CreateTraineeRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "trainers", source = "trainers")
    TraineeProfileResponse toProfileResponse(Trainee trainee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.userId", ignore = true)
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.active", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    void updateFromRequest(UpdateTraineeRequest request, @MappingTarget Trainee trainee);
}
