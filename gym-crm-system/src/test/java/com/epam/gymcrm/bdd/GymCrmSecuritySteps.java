package com.epam.gymcrm.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public class GymCrmSecuritySteps {

  private final RestClient restClient;
  private int responseStatus;

  public GymCrmSecuritySteps(@LocalServerPort int port) {
    this.restClient = RestClient.create("http://localhost:" + port);
  }

  @When("the client requests trainee profile for {string} without a JWT")
  public void requestTraineeProfileWithoutJwt(String username) {
    try {
      ResponseEntity<Void> response =
          restClient
              .get()
              .uri("/api/v1/trainees/profile?username={username}", username)
              .retrieve()
              .toBodilessEntity();
      responseStatus = response.getStatusCode().value();
    } catch (RestClientResponseException exception) {
      responseStatus = exception.getStatusCode().value();
    }
  }

  @Then("the response status should be {int}")
  public void responseStatusShouldBe(int expectedStatus) {
    assertThat(responseStatus).isEqualTo(expectedStatus);
  }
}
