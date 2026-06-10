package com.epam.gymcrm.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.web.auth.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityAuthorizationTest extends PostgresContainerTest {

  private final RestClient restClient;
  private final JwtTokenService jwtTokenService;

  @Autowired
  SecurityAuthorizationTest(@LocalServerPort int port, JwtTokenService jwtTokenService) {
    this.restClient = RestClient.create("http://localhost:" + port);
    this.jwtTokenService = jwtTokenService;
  }

  @Test
  void traineeEndpointsShouldRejectTrainerRole() {
    String token = jwtTokenService.createToken("Mike.Stone", ProfileType.TRAINER);

    HttpClientErrorException.Forbidden exception =
        catchThrowableOfType(
            () ->
                restClient
                    .get()
                    .uri("/api/v1/trainees/profile?username=John.Doe")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity(),
            HttpClientErrorException.Forbidden.class);

    assertThat(exception.getResponseBodyAsString()).isEqualTo("{\"message\":\"Access is denied\"}");
  }

  @Test
  void trainerEndpointsShouldRejectTraineeRole() {
    String token = jwtTokenService.createToken("John.Doe", ProfileType.TRAINEE);

    HttpClientErrorException.Forbidden exception =
        catchThrowableOfType(
            () ->
                restClient
                    .get()
                    .uri("/api/v1/trainers/profile?username=Mike.Stone")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity(),
            HttpClientErrorException.Forbidden.class);

    assertThat(exception.getResponseBodyAsString()).isEqualTo("{\"message\":\"Access is denied\"}");
  }

  @Test
  void addTrainingShouldRejectTrainerRole() {
    String token = jwtTokenService.createToken("Mike.Stone", ProfileType.TRAINER);

    HttpClientErrorException.Forbidden exception =
        catchThrowableOfType(
            () ->
                restClient
                    .post()
                    .uri("/api/v1/trainings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(token))
                    .body(validAddTrainingRequest())
                    .retrieve()
                    .toBodilessEntity(),
            HttpClientErrorException.Forbidden.class);

    assertThat(exception.getResponseBodyAsString()).isEqualTo("{\"message\":\"Access is denied\"}");
  }

  @Test
  void protectedReferenceEndpointShouldRejectMissingToken() {
    HttpClientErrorException.Unauthorized exception =
        catchThrowableOfType(
            () ->
                restClient.get().uri("/api/v1/training-types").retrieve().toBodilessEntity(),
            HttpClientErrorException.Unauthorized.class);

    assertThat(exception.getResponseBodyAsString())
        .isEqualTo("{\"message\":\"Authentication is required\"}");
  }

  @Test
  void protectedEndpointShouldRejectTokenAfterLogout() {
    String token = jwtTokenService.createToken("John.Doe", ProfileType.TRAINEE);

    ResponseEntity<Void> logoutResponse =
        restClient
            .post()
            .uri("/api/v1/auth/logout")
            .headers(headers -> headers.setBearerAuth(token))
            .retrieve()
            .toBodilessEntity();

    HttpClientErrorException.Unauthorized exception =
        catchThrowableOfType(
            () ->
                restClient
                    .get()
                    .uri("/api/v1/training-types")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity(),
            HttpClientErrorException.Unauthorized.class);

    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(exception.getResponseBodyAsString()).contains("JWT has been revoked");
  }

  private static String validAddTrainingRequest() {
    return
        """
        {
          "traineeUsername": "John.Doe",
          "trainerUsername": "Mike.Stone",
          "trainingName": "Morning Training",
          "trainingTypeName": "Fitness",
          "trainingDate": "2026-01-10",
          "trainingDuration": 60
        }
        """;
  }
}
