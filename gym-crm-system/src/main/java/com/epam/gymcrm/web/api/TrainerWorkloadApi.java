package com.epam.gymcrm.web.api;

import com.epam.gymcrm.dto.workload.TrainerWorkloadResponse;
import com.epam.gymcrm.web.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

/** OpenAPI contract for trainer workload endpoints. */
@Tag(name = "Trainer Workloads", description = "Trainer workload summary operations")
public interface TrainerWorkloadApi {

  /** Gets trainer workload summary from the internal workload service. */
  @Operation(summary = "Get trainer workload")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Trainer workload found",
        content = @Content(schema = @Schema(implementation = TrainerWorkloadResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(
        responseCode = "404",
        description = "Trainer workload not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  TrainerWorkloadResponse getTrainerWorkload(@PathVariable String username);
}
