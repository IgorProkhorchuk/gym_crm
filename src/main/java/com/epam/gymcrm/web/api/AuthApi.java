package com.epam.gymcrm.web.api;

import com.epam.gymcrm.dto.auth.LoginRequest;
import com.epam.gymcrm.dto.auth.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/** OpenAPI contract for authentication endpoints. */
@Tag(name = "Authentication", description = "Fake token authentication endpoints")
public interface AuthApi {

  /** Authenticates a trainee or trainer and returns a fake token. */
  @Operation(summary = "Login user", description = "Authenticates by username and password.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User authenticated",
        content = @Content(schema = @Schema(implementation = LoginResponse.class))),
    @ApiResponse(responseCode = "401", description = "Invalid username or password")
  })
  LoginResponse loginUser(
      @RequestBody(
              required = true,
              description = "Username and password",
              content = @Content(schema = @Schema(implementation = LoginRequest.class)))
          LoginRequest loginRequest);
}
