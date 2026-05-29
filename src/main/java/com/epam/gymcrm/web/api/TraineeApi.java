package com.epam.gymcrm.web.api;

import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.web.dto.ChangePasswordRestRequest;
import com.epam.gymcrm.web.dto.DeleteProfileRestRequest;
import com.epam.gymcrm.web.dto.SwitchProfileStatusRestRequest;
import com.epam.gymcrm.web.dto.UpdateTraineeProfileRestRequest;
import com.epam.gymcrm.web.dto.UpdateTraineeTrainersRestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;

/** OpenAPI contract for trainee endpoints. */
@Tag(name = "Trainees", description = "Trainee profile and trainee-owned operations")
public interface TraineeApi {

  /** Registers a trainee profile. */
  @Operation(summary = "Register trainee profile")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Trainee profile created",
        content = @Content(schema = @Schema(implementation = UsernamePasswordResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid registration request")
  })
  UsernamePasswordResponse createTrainee(
      @RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = CreateTraineeRequest.class)))
          CreateTraineeRequest request);

  /** Returns the authenticated trainee profile. */
  @Operation(summary = "Get trainee profile")
  @SecurityRequirement(name = "tokenAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Trainee profile",
        content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  TraineeProfileResponse getTraineeProfile(
      @Parameter(description = "Authentication token", required = true) String token,
      @Parameter(description = "Trainee username", required = true) String username);

  /** Updates the authenticated trainee profile. */
  @Operation(summary = "Update trainee profile")
  @SecurityRequirement(name = "tokenAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Updated trainee profile",
        content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid update request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  TraineeProfileResponse updateTraineeProfile(
      @Parameter(description = "Authentication token", required = true) String token,
      @RequestBody(
              required = true,
              content =
                  @Content(schema = @Schema(implementation = UpdateTraineeProfileRestRequest.class)))
          UpdateTraineeProfileRestRequest traineeRequest);

  /** Deletes the authenticated trainee profile. */
  @Operation(summary = "Delete trainee profile")
  @SecurityRequirement(name = "tokenAuth")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Trainee profile deleted"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  void deleteProfile(
      @Parameter(description = "Authentication token", required = true) String token,
      @RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = DeleteProfileRestRequest.class)))
          DeleteProfileRestRequest request);

  /** Changes the authenticated trainee password. */
  @Operation(summary = "Change trainee password")
  @SecurityRequirement(name = "tokenAuth")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Password changed"),
    @ApiResponse(responseCode = "400", description = "Invalid password change request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  void changePassword(
      @Parameter(description = "Authentication token", required = true) String token,
      @RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = ChangePasswordRestRequest.class)))
          ChangePasswordRestRequest body);

  /** Switches the authenticated trainee active status. */
  @Operation(summary = "Activate or deactivate trainee profile")
  @SecurityRequirement(name = "tokenAuth")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Trainee active status switched"),
    @ApiResponse(responseCode = "400", description = "Invalid status request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  void switchActiveStatus(
      @Parameter(description = "Authentication token", required = true) String token,
      @RequestBody(
              required = true,
              content =
                  @Content(schema = @Schema(implementation = SwitchProfileStatusRestRequest.class)))
          SwitchProfileStatusRestRequest request);

  /** Returns active trainers not assigned to the authenticated trainee. */
  @Operation(summary = "Get not assigned active trainers")
  @SecurityRequirement(name = "tokenAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Trainer list",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = TrainerSummaryResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  List<TrainerSummaryResponse> getUnassignedTrainers(
      @Parameter(description = "Authentication token", required = true) String token,
      @Parameter(description = "Trainee username", required = true) String username);

  /** Replaces the authenticated trainee trainer list. */
  @Operation(summary = "Update trainee trainer list")
  @SecurityRequirement(name = "tokenAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Updated trainer list",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = TrainerSummaryResponse.class)))),
    @ApiResponse(responseCode = "400", description = "Invalid trainer list request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  List<TrainerSummaryResponse> updateTraineeTrainers(
      @Parameter(description = "Authentication token", required = true) String token,
      @RequestBody(
              required = true,
              content =
                  @Content(schema = @Schema(implementation = UpdateTraineeTrainersRestRequest.class)))
          UpdateTraineeTrainersRestRequest request);

  /** Returns trainings for the authenticated trainee. */
  @Operation(summary = "Get trainee trainings list")
  @SecurityRequirement(name = "tokenAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Trainee trainings",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = TraineeTrainingResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  List<TraineeTrainingResponse> getTraineeTrainings(
      @Parameter(description = "Authentication token", required = true) String token,
      @Parameter(description = "Trainee username", required = true) String username,
      @Parameter(description = "Training period start date") LocalDate fromDate,
      @Parameter(description = "Training period end date") LocalDate toDate,
      @Parameter(description = "Trainer name filter") String trainerName,
      @Parameter(description = "Training type filter") String trainingType);
}
