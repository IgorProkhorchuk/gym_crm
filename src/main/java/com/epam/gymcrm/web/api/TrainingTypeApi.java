package com.epam.gymcrm.web.api;

import com.epam.gymcrm.dto.training.TrainingTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

/** OpenAPI contract for training type endpoints. */
@Tag(name = "Training Types", description = "Read-only training type reference data")
public interface TrainingTypeApi {

  /** Returns all available training types. */
  @Operation(summary = "Get training types")
  @SecurityRequirement(name = "tokenAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Training types",
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = TrainingTypeResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  List<TrainingTypeResponse> getTrainingTypes(
      @Parameter(description = "Authentication token", required = true) String token);
}
