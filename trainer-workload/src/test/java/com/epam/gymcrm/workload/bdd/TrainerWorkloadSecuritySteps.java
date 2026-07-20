package com.epam.gymcrm.workload.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.workload.dto.TrainerWorkloadMonthResponse;
import com.epam.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.epam.gymcrm.workload.dto.TrainerWorkloadYearResponse;
import com.epam.gymcrm.workload.exception.TrainerWorkloadNotFoundException;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClientResponseException;

public class TrainerWorkloadSecuritySteps {

  private final RestClient restClient;
  private final TrainerWorkloadService trainerWorkloadService;
  private final TestJwtFactory testJwtFactory;
  private int responseStatus;
  private TrainerWorkloadResponse responseBody;

  public TrainerWorkloadSecuritySteps(
      @LocalServerPort int port,
      TrainerWorkloadService trainerWorkloadService,
      TestJwtFactory testJwtFactory
  ) {
    this.restClient = RestClient.create("http://localhost:" + port);
    this.trainerWorkloadService = trainerWorkloadService;
    this.testJwtFactory = testJwtFactory;
  }

  @When("the client requests trainer workload for {string} using {string}")
  public void requestTrainerWorkloadUsingCredential(String username, String credential) {
    requestTrainerWorkload(username, tokenForCredential(credential));
  }

  @When("the service client requests missing trainer workload for {string} with a service JWT")
  public void requestMissingTrainerWorkloadWithServiceJwt(String username) {
    when(trainerWorkloadService.getTrainerWorkload(username))
        .thenThrow(new TrainerWorkloadNotFoundException(username));

    requestTrainerWorkload(username, testJwtFactory.serviceToken());
  }

  @When("the service client requests existing trainer workload for {string} with a service JWT")
  public void requestExistingTrainerWorkloadWithServiceJwt(String username) {
    TrainerWorkloadResponse workload =
        new TrainerWorkloadResponse(
            username,
            "Mike",
            "Stone",
            true,
            List.of(
                new TrainerWorkloadYearResponse(
                    2026,
                    List.of(new TrainerWorkloadMonthResponse(7, 120)))));

    when(trainerWorkloadService.getTrainerWorkload(username)).thenReturn(workload);

    requestTrainerWorkload(username, testJwtFactory.serviceToken());
  }

  @Then("the response status should be {int}")
  public void responseStatusShouldBe(int expectedStatus) {
    assertThat(responseStatus).isEqualTo(expectedStatus);
  }

  @Then("the response should contain trainer username {string}")
  public void responseShouldContainTrainerUsername(String expectedUsername) {
    assertThat(responseBody).isNotNull();
    assertThat(responseBody.trainerUsername()).isEqualTo(expectedUsername);
  }

  @Then("the response should contain workload duration {int} minutes for year {int} and month {int}")
  public void responseShouldContainWorkloadDuration(
      int expectedDuration,
      int expectedYear,
      int expectedMonth
  ) {
    assertThat(responseBody).isNotNull();
    assertThat(responseBody.years())
        .anySatisfy(
            year -> assertAll(
                () -> assertThat(year.year()).isEqualTo(expectedYear),
                () -> assertThat(year.months())
                    .anySatisfy(
                        month -> assertAll(
                            () -> assertThat(month.month()).isEqualTo(expectedMonth),
                            () -> assertThat(month.trainingSummaryDuration())
                                .isEqualTo(expectedDuration)))));
  }

  private void requestTrainerWorkload(String username, String token) {
    RequestHeadersSpec<?> request =
        restClient.get().uri("/v1/trainer-workloads/{username}", username);
    if (token != null) {
      request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    try {
      ResponseEntity<TrainerWorkloadResponse> response =
          request.retrieve().toEntity(TrainerWorkloadResponse.class);
      responseStatus = response.getStatusCode().value();
      responseBody = response.getBody();
    } catch (RestClientResponseException exception) {
      responseStatus = exception.getStatusCode().value();
      responseBody = null;
    }
  }

  private String tokenForCredential(String credential) {
    return switch (credential) {
      case "no JWT" -> null;
      case "USER JWT" -> testJwtFactory.tokenWithRole("USER");
      case "SERVICE JWT" -> testJwtFactory.serviceToken();
      default -> throw new IllegalArgumentException("Unsupported credential: " + credential);
    };
  }
}
