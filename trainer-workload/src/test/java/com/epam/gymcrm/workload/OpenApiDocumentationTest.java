package com.epam.gymcrm.workload;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.data.mongodb.repositories.enabled=false",
        "spring.data.mongodb.auto-index-creation=false",
        "spring.autoconfigure.exclude="
            + "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
            + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
            + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration"
    })
class OpenApiDocumentationTest {

  @MockitoBean private TrainerWorkloadService trainerWorkloadService;

  private final RestClient restClient;

  OpenApiDocumentationTest(@LocalServerPort int port) {
    this.restClient = RestClient.create("http://localhost:" + port);
  }

  @Test
  void apiDocsShouldExposeOpenApiContract() {
    String apiDocs = restClient.get().uri("/v3/api-docs").retrieve().body(String.class);

    assertThat(apiDocs)
        .contains("\"title\":\"Trainer Workload REST API\"")
        .contains("\"bearerAuth\"")
        .contains("\"scheme\":\"bearer\"")
        .contains("\"bearerFormat\":\"JWT\"")
        .contains("\"/v1/trainer-workloads/{username}\"")
        .doesNotContain("\"/v1/trainer-workloads\"")
        .contains("\"Trainer Workloads\"");
  }

  @Test
  void swaggerUiShouldRedirectToIndexPage() {
    ResponseEntity<String> response =
        restClient.get().uri("/swagger-ui.html").retrieve().toEntity(String.class);

    assertThat(response.getStatusCode()).isIn(HttpStatus.FOUND, HttpStatus.OK);
    if (response.getStatusCode().is3xxRedirection()) {
      assertThat(response.getHeaders().getLocation().toString()).contains("/swagger-ui/index.html");
    } else {
      assertThat(response.getBody()).contains("Swagger UI");
    }
  }
}
