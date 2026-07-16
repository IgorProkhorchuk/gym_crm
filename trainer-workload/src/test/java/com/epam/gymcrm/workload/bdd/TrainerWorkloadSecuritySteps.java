package com.epam.gymcrm.workload.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

public class TrainerWorkloadSecuritySteps {

  private final RestClient restClient;
  private HttpClientErrorException responseException;

  public TrainerWorkloadSecuritySteps(@LocalServerPort int port) {
    this.restClient = RestClient.create("http://localhost:" + port);
  }

  @When("the client requests trainer workload without a JWT")
  public void requestTrainerWorkloadWithoutJwt() {
    responseException =
        catchThrowableOfType(
            () ->
                restClient
                    .get()
                    .uri("/api/v1/trainer-workloads/Mike.Stone")
                    .retrieve()
                    .toBodilessEntity(),
            HttpClientErrorException.class);
  }

  @Then("the response status should be {int}")
  public void responseStatusShouldBe(int expectedStatus) {
    assertThat(responseException).isNotNull();
    assertThat(responseException.getStatusCode().value()).isEqualTo(expectedStatus);
  }
}
