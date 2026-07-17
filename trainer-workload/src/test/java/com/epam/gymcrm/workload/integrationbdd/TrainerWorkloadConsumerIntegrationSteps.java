package com.epam.gymcrm.workload.integrationbdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.messaging.TrainerWorkloadMessageListener;
import com.epam.gymcrm.workload.model.TrainerWorkload;
import com.epam.gymcrm.workload.model.TrainerWorkloadMonthSummary;
import com.epam.gymcrm.workload.model.TrainerWorkloadProcessedEvent;
import com.epam.gymcrm.workload.model.TrainerWorkloadYearSummary;
import com.epam.gymcrm.workload.repository.TrainerWorkloadProcessedEventRepository;
import com.epam.gymcrm.workload.repository.TrainerWorkloadRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;

public class TrainerWorkloadConsumerIntegrationSteps {

  private final TrainerWorkloadMessageListener listener;
  private final TrainerWorkloadRepository trainerWorkloadRepository;
  private final TrainerWorkloadProcessedEventRepository processedEventRepository;

  public TrainerWorkloadConsumerIntegrationSteps(
      TrainerWorkloadMessageListener listener,
      TrainerWorkloadRepository trainerWorkloadRepository,
      TrainerWorkloadProcessedEventRepository processedEventRepository
  ) {
    this.listener = listener;
    this.trainerWorkloadRepository = trainerWorkloadRepository;
    this.processedEventRepository = processedEventRepository;
  }

  @Before
  public void cleanCollections() {
    trainerWorkloadRepository.deleteAll();
    processedEventRepository.deleteAll();
  }

  @When(
      "the workload listener receives an {string} integration event with training id {long} "
          + "for trainer {string} on {string} with duration {int}")
  public void workloadListenerReceivesIntegrationEvent(
      String actionType,
      long trainingId,
      String trainerUsername,
      String trainingDate,
      int duration
  ) {
    listener.handleMessage(
        payload(actionType, trainingId, trainerUsername, trainingDate, duration),
        "bdd-integration-transaction-id",
        1);
  }

  @Then("the trainer workload for {string} should contain {int} minutes for year {int} and month {int}")
  public void trainerWorkloadShouldContainDuration(
      String trainerUsername,
      int expectedDuration,
      int expectedYear,
      int expectedMonth
  ) {
    TrainerWorkload workload =
        trainerWorkloadRepository.findByUsername(trainerUsername).orElseThrow();
    TrainerWorkloadYearSummary yearSummary =
        workload.getYears().stream()
            .filter(year -> year.getYear() == expectedYear)
            .findFirst()
            .orElseThrow();
    TrainerWorkloadMonthSummary monthSummary =
        yearSummary.getMonths().stream()
            .filter(month -> month.getMonth() == expectedMonth)
            .findFirst()
            .orElseThrow();

    assertThat(workload.getFirstName()).isEqualTo("Integration");
    assertThat(workload.getLastName()).isEqualTo("Trainer");
    assertThat(workload.isActive()).isTrue();
    assertThat(monthSummary.getTrainingsSummaryDuration()).isEqualTo(expectedDuration);
  }

  @Then("the processed workload event with training id {long} and action {string} should be recorded")
  public void processedWorkloadEventShouldBeRecorded(long trainingId, String actionType) {
    assertThat(processedEventRepository.findAll())
        .filteredOn(event -> eventMatches(event, trainingId, actionType))
        .hasSize(1);
  }

  private static boolean eventMatches(
      TrainerWorkloadProcessedEvent event,
      long trainingId,
      String actionType
  ) {
    return event.getTrainingId().equals(trainingId)
        && event.getActionType() == ActionType.valueOf(actionType);
  }

  private static String payload(
      String actionType,
      long trainingId,
      String trainerUsername,
      String trainingDate,
      int duration
  ) {
    LocalDate.parse(trainingDate);
    return
        """
        {
          "trainingId": %d,
          "trainerUsername": "%s",
          "trainerFirstName": "Integration",
          "trainerLastName": "Trainer",
          "trainerStatus": true,
          "trainingDate": "%s",
          "trainingDuration": %d,
          "actionType": "%s"
        }
        """
            .formatted(trainingId, trainerUsername, trainingDate, duration, actionType);
  }
}
