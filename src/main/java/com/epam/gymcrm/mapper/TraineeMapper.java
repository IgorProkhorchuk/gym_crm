package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.model.Trainee;
import org.mapstruct.BeanMapping;
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

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.active", constant = "true")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    Trainee toEntity(CreateTraineeRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "trainers", source = "trainers")
    TraineeProfileResponse toProfileResponse(Trainee trainee);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    void updateFromRequest(UpdateTraineeRequest request, @MappingTarget Trainee trainee);
}
