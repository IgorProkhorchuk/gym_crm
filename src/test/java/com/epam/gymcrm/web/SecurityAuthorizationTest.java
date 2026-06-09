package com.epam.gymcrm.web;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.web.auth.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
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

    assertThatThrownBy(
            () ->
                restClient
                    .get()
                    .uri("/api/v1/trainees/profile?username=John.Doe")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity())
        .isInstanceOf(HttpClientErrorException.Forbidden.class);
  }

  @Test
  void trainerEndpointsShouldRejectTraineeRole() {
    String token = jwtTokenService.createToken("John.Doe", ProfileType.TRAINEE);

    assertThatThrownBy(
            () ->
                restClient
                    .get()
                    .uri("/api/v1/trainers/profile?username=Mike.Stone")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity())
        .isInstanceOf(HttpClientErrorException.Forbidden.class);
  }

  @Test
  void addTrainingShouldRejectTrainerRole() {
    String token = jwtTokenService.createToken("Mike.Stone", ProfileType.TRAINER);

    assertThatThrownBy(
            () ->
                restClient
                    .post()
                    .uri("/api/v1/trainings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(token))
                    .body(validAddTrainingRequest())
                    .retrieve()
                    .toBodilessEntity())
        .isInstanceOf(HttpClientErrorException.Forbidden.class);
  }

  @Test
  void protectedReferenceEndpointShouldRejectMissingToken() {
    assertThatThrownBy(
            () ->
                restClient.get().uri("/api/v1/training-types").retrieve().toBodilessEntity())
        .isInstanceOf(HttpClientErrorException.Unauthorized.class);
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
