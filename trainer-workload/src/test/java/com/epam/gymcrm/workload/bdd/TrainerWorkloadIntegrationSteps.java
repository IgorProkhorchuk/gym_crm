package com.epam.gymcrm.workload.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.boot.test.web.server.LocalServerPort;

public class TrainerWorkloadIntegrationSteps {

  private final TrainerWorkloadService trainerWorkloadService;

  @LocalServerPort private int localServerPort;

  public TrainerWorkloadIntegrationSteps(TrainerWorkloadService trainerWorkloadService) {
    this.trainerWorkloadService = trainerWorkloadService;
  }

  @Given("the trainer workload integration context is ready")
  public void trainerWorkloadIntegrationContextIsReady() {
    assertThat(trainerWorkloadService).isNotNull();
  }

  @Then("the trainer workload REST API should be available in the test context")
  public void trainerWorkloadRestApiShouldBeAvailableInTheTestContext() {
    assertThat(localServerPort).isPositive();
  }
}
