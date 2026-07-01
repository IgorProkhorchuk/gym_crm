package com.epam.gymcrm.workload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityAuthorizationTest {

  private final RestClient restClient;

  SecurityAuthorizationTest(@LocalServerPort int port) {
    this.restClient = RestClient.create("http://localhost:" + port);
  }

  @Test
  void trainerWorkloadGetEndpointShouldRejectMissingServiceToken() {
    HttpClientErrorException.Unauthorized exception =
        catchThrowableOfType(
            () ->
                restClient
                    .get()
                    .uri("/api/v1/trainer-workloads/Mike.Stone")
                    .retrieve()
                    .toBodilessEntity(),
            HttpClientErrorException.Unauthorized.class);

    assertThat(exception.getStatusCode().value()).isEqualTo(401);
    assertThat(exception.getResponseBodyAsString()).contains("message");
  }
}
