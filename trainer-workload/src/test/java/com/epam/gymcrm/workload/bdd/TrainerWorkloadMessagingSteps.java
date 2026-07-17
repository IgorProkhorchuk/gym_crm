package com.epam.gymcrm.workload.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.messaging.TrainerWorkloadMessageListener;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import org.mockito.ArgumentCaptor;

public class TrainerWorkloadMessagingSteps {

  private final TrainerWorkloadMessageListener listener;
  private final TrainerWorkloadService trainerWorkloadService;
  private TrainerWorkloadRequest handledRequest;
  private RuntimeException listenerException;

  public TrainerWorkloadMessagingSteps(
      TrainerWorkloadMessageListener listener,
      TrainerWorkloadService trainerWorkloadService
  ) {
    this.listener = listener;
    this.trainerWorkloadService = trainerWorkloadService;
  }

  @When("the workload listener receives an {string} event for trainer {string} with duration {int}")
  public void workloadListenerReceivesEvent(
      String actionType,
      String trainerUsername,
      int duration
  ) {
    reset(trainerWorkloadService);
    listenerException = null;

    listener.handleMessage(
        validPayload(actionType, trainerUsername, duration),
        "bdd-transaction-id",
        1);

    ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
        ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
    verify(trainerWorkloadService).updateTrainerWorkload(requestCaptor.capture());
    handledRequest = requestCaptor.getValue();
  }

  @When("the workload listener receives an invalid {string} event for trainer {string} with duration {int}")
  public void workloadListenerReceivesInvalidEvent(
      String actionType,
      String trainerUsername,
      int duration
  ) {
    reset(trainerWorkloadService);
    handledRequest = null;

    try {
      listener.handleMessage(
          validPayload(actionType, trainerUsername, duration),
          "bdd-transaction-id",
          1);
    } catch (RuntimeException exception) {
      listenerException = exception;
    }
  }

  @Then("the workload service should receive an {string} request for trainer {string}")
  public void workloadServiceShouldReceiveRequest(String expectedActionType, String expectedUsername) {
    assertThat(handledRequest).isNotNull();
    assertThat(handledRequest.actionType()).isEqualTo(ActionType.valueOf(expectedActionType));
    assertThat(handledRequest.trainerUsername()).isEqualTo(expectedUsername);
  }

  @Then("the workload request should contain duration {int} minutes for date {string}")
  public void workloadRequestShouldContainDuration(int expectedDuration, String expectedDate) {
    assertThat(handledRequest).isNotNull();
    assertThat(handledRequest.trainingDuration()).isEqualTo(expectedDuration);
    assertThat(handledRequest.trainingDate()).isEqualTo(LocalDate.parse(expectedDate));
  }

  @Then("the workload message should be rejected with error {string}")
  public void workloadMessageShouldBeRejectedWithError(String expectedErrorType) {
    assertThat(listenerException).isNotNull();
    assertThat(listenerException.getClass().getSimpleName()).isEqualTo(expectedErrorType);
  }

  @Then("the workload service should not receive a workload request")
  public void workloadServiceShouldNotReceiveWorkloadRequest() {
    verifyNoInteractions(trainerWorkloadService);
  }

  private static String validPayload(String actionType, String trainerUsername, int duration) {
    return
        """
        {
          "trainingId": 10,
          "trainerUsername": "%s",
          "trainerFirstName": "Training",
          "trainerLastName": "Trainer",
          "trainerStatus": true,
          "trainingDate": "2026-05-03",
          "trainingDuration": %d,
          "actionType": "%s"
        }
        """
            .formatted(trainerUsername, duration, actionType);
  }
}
