package com.epam.gymcrm.workload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.data.mongodb.repositories.enabled=false",
        "spring.data.mongodb.auto-index-creation=false",
        "spring.autoconfigure.exclude="
            + "org.springframework.boot.data.mongodb.autoconfigure.MongoAutoConfiguration,"
            + "org.springframework.boot.data.mongodb.autoconfigure.MongoDataAutoConfiguration,"
            + "org.springframework.boot.data.mongodb.autoconfigure.MongoRepositoriesAutoConfiguration"
    })
class SecurityAuthorizationTest {

  @MockitoBean private TrainerWorkloadService trainerWorkloadService;

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
