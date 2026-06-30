package com.epam.gymcrm.workload.api;

import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.epam.gymcrm.workload.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

/** OpenAPI contract for trainer workload endpoints. */
@Tag(name = "Trainer Workloads", description = "Trainer workload summary operations")
public interface TrainerWorkloadApi {

  /** Updates trainer monthly workload summary. */
  @Operation(
      summary = "Update trainer workload",
      description = "Applies ADD or DELETE training action to trainer monthly workload summary.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Trainer workload updated"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid workload update request",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "Missing or invalid service JWT")
  })
  ResponseEntity<Void> updateTrainerWorkload(
      @RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = TrainerWorkloadRequest.class)))
          TrainerWorkloadRequest request);

  /** Returns trainer workload summary grouped by years and months. */
  @Operation(summary = "Get trainer workload")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Trainer workload found",
        content = @Content(schema = @Schema(implementation = TrainerWorkloadResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Trainer workload not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(@PathVariable String username);
}
