package com.epam.gymcrm.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.web.auth.JwtTokenService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClientResponseException;

public class GymCrmSecuritySteps {

  private final RestClient restClient;
  private final JwtTokenService jwtTokenService;
  private int responseStatus;

  public GymCrmSecuritySteps(@LocalServerPort int port, JwtTokenService jwtTokenService) {
    this.restClient = RestClient.create("http://localhost:" + port);
    this.jwtTokenService = jwtTokenService;
  }

  @When("the client requests trainee profile for {string} using {string}")
  public void requestTraineeProfileUsingCredential(String username, String credential) {
    requestTraineeProfile(username, tokenForCredential(credential));
  }

  @Then("the response status should be {int}")
  public void responseStatusShouldBe(int expectedStatus) {
    assertThat(responseStatus).isEqualTo(expectedStatus);
  }

  private void requestTraineeProfile(String username, String token) {
    RequestHeadersSpec<?> request =
        restClient.get().uri("/api/v1/trainees/profile?username={username}", username);
    if (token != null) {
      request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    try {
      ResponseEntity<Void> response = request.retrieve().toBodilessEntity();
      responseStatus = response.getStatusCode().value();
    } catch (RestClientResponseException exception) {
      responseStatus = exception.getStatusCode().value();
    }
  }

  private String tokenForCredential(String credential) {
    return switch (credential) {
      case "no JWT" -> null;
      case "TRAINEE JWT" -> jwtTokenService.createToken("John.Doe", ProfileType.TRAINEE);
      case "TRAINER JWT" -> jwtTokenService.createToken("Mike.Stone", ProfileType.TRAINER);
      default -> throw new IllegalArgumentException("Unsupported credential: " + credential);
    };
  }
}
