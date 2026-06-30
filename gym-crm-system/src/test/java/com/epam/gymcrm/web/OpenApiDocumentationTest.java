package com.epam.gymcrm.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocumentationTest extends PostgresContainerTest {

  private final RestClient restClient;

  OpenApiDocumentationTest(@LocalServerPort int port) {
    this.restClient = RestClient.create("http://localhost:" + port);
  }

  @Test
  void apiDocsShouldExposeOpenApiContract() {
    String apiDocs = restClient.get().uri("/api/v3/api-docs").retrieve().body(String.class);

    assertThat(apiDocs)
        .contains("\"title\":\"Gym CRM REST API\"")
        .contains("\"bearerAuth\"")
        .contains("\"scheme\":\"bearer\"")
        .contains("\"bearerFormat\":\"JWT\"")
        .contains("\"/v1/auth/login\"")
        .contains("\"/v1/auth/logout\"")
        .contains("\"/v1/trainer-workloads/{username}\"")
        .contains("\"423\"");
  }

  @Test
  void swaggerUiShouldRedirectToIndexPage() {
    ResponseEntity<String> response =
        restClient.get().uri("/api/swagger-ui.html").retrieve().toEntity(String.class);

    assertThat(response.getStatusCode()).isIn(HttpStatus.FOUND, HttpStatus.OK);
    if (response.getStatusCode().is3xxRedirection()) {
      assertThat(response.getHeaders().getLocation().toString()).contains("/api/swagger-ui/index.html");
    } else {
      assertThat(response.getBody()).contains("Swagger UI");
    }
  }
}
