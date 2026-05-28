package com.epam.gymcrm.web.api;

import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.web.dto.ChangePasswordRestRequest;
import com.epam.gymcrm.web.dto.SwitchProfileStatusRestRequest;
import com.epam.gymcrm.web.dto.UpdateTrainerProfileRestRequest;
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

/** OpenAPI contract for trainer endpoints. */
@Tag(name = "Trainers", description = "Trainer profile and trainer-owned operations")
public interface TrainerApi {

  /** Registers a trainer profile. */
  @Operation(summary = "Register trainer profile")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Trainer profile created",
        content = @Content(schema = @Schema(implementation = UsernamePasswordResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid registration request")
  })
  UsernamePasswordResponse createTrainer(
      @RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = CreateTrainerRequest.class)))
          CreateTrainerRequest request);

  /** Returns the authenticated trainer profile. */
  @Operation(summary = "Get trainer profile")
  @SecurityRequirement(name = "fakeTokenAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Trainer profile",
        content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  TrainerProfileResponse getTrainerProfile(
      @Parameter(description = "Fake authentication token", required = true) String token,
      @Parameter(description = "Trainer username", required = true) String username);

  /** Updates the authenticated trainer profile. */
  @Operation(summary = "Update trainer profile")
  @SecurityRequirement(name = "fakeTokenAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Updated trainer profile",
        content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid update request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  TrainerProfileResponse updateTrainerProfile(
      @Parameter(description = "Fake authentication token", required = true) String token,
      @RequestBody(
              required = true,
              content =
                  @Content(schema = @Schema(implementation = UpdateTrainerProfileRestRequest.class)))
          UpdateTrainerProfileRestRequest trainerRequest);

  /** Changes the authenticated trainer password. */
  @Operation(summary = "Change trainer password")
  @SecurityRequirement(name = "fakeTokenAuth")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Password changed"),
    @ApiResponse(responseCode = "400", description = "Invalid password change request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  void changePassword(
      @Parameter(description = "Fake authentication token", required = true) String token,
      @RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = ChangePasswordRestRequest.class)))
          ChangePasswordRestRequest body);

  /** Switches the authenticated trainer active status. */
  @Operation(summary = "Activate or deactivate trainer profile")
  @SecurityRequirement(name = "fakeTokenAuth")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Trainer active status switched"),
    @ApiResponse(responseCode = "400", description = "Invalid status request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  void switchActiveStatus(
      @Parameter(description = "Fake authentication token", required = true) String token,
      @RequestBody(
              required = true,
              content =
                  @Content(schema = @Schema(implementation = SwitchProfileStatusRestRequest.class)))
          SwitchProfileStatusRestRequest request);

  /** Returns trainings for the authenticated trainer. */
  @Operation(summary = "Get trainer trainings list")
  @SecurityRequirement(name = "fakeTokenAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Trainer trainings",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = TrainerTrainingResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  List<TrainerTrainingResponse> getTrainerTrainings(
      @Parameter(description = "Fake authentication token", required = true) String token,
      @Parameter(description = "Trainer username", required = true) String username,
      @Parameter(description = "Training period start date") LocalDate fromDate,
      @Parameter(description = "Training period end date") LocalDate toDate,
      @Parameter(description = "Trainee name filter") String traineeName);
}
