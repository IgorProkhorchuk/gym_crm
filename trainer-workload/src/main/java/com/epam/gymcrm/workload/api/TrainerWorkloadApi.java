package com.epam.gymcrm.workload.api;

import com.epam.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.epam.gymcrm.workload.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

/** OpenAPI contract for trainer workload endpoints. */
@Tag(name = "Trainer Workloads", description = "Trainer workload summary operations")
public interface TrainerWorkloadApi {

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
