package com.epam.gymcrm.web.api;

import com.epam.gymcrm.dto.auth.LoginRequest;
import com.epam.gymcrm.dto.auth.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** OpenAPI contract for authentication endpoints. */
@Tag(name = "Authentication", description = "JWT authentication endpoints")
public interface AuthApi {

  /** Authenticates a trainee or trainer and returns a JWT bearer token. */
  @Operation(summary = "Login user", description = "Authenticates by username and password.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User authenticated",
        content = @Content(schema = @Schema(implementation = LoginResponse.class))),
    @ApiResponse(responseCode = "401", description = "Invalid username or password"),
    @ApiResponse(responseCode = "423", description = "User is temporarily blocked")
  })
  LoginResponse loginUser(
      @RequestBody(
              required = true,
              description = "Username and password",
              content = @Content(schema = @Schema(implementation = LoginRequest.class)))
          LoginRequest loginRequest);

  /** Revokes the current JWT bearer token. */
  @Operation(summary = "Logout user", description = "Revokes the current JWT bearer token.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "User logged out"),
    @ApiResponse(responseCode = "401", description = "Missing, invalid, or revoked JWT")
  })
  void logoutUser();
}
