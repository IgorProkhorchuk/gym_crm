package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.model.Trainer;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        builder = @Builder(disableBuilder = true)
)
public interface TrainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.userId", ignore = true)
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.active", source = "active")
    @Mapping(target = "specialization.trainingTypeId", ignore = true)
    @Mapping(target = "specialization.trainingTypeName", source = "specialization")
    @Mapping(target = "trainees", ignore = true)
    Trainer toEntity(CreateTrainerRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    TrainerProfileResponse toProfileResponse(Trainer trainer);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    TrainerSummaryResponse toSummaryResponse(Trainer trainer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.userId", ignore = true)
    @Mapping(target = "user.username", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.active", ignore = true)
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    void updateFromRequest(UpdateTrainerRequest request, @MappingTarget Trainer trainer);
}
