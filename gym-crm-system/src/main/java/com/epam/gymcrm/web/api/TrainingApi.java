package com.epam.gymcrm.web.api;

import com.epam.gymcrm.web.dto.AddTrainingRestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** OpenAPI contract for training endpoints. */
@Tag(name = "Trainings", description = "Training management operations")
public interface TrainingApi {

  /** Adds a training for the authenticated trainee. */
  @Operation(summary = "Add training")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Training added"),
    @ApiResponse(responseCode = "400", description = "Invalid training request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  void addTraining(
      @RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = AddTrainingRestRequest.class)))
          AddTrainingRestRequest request);
}
