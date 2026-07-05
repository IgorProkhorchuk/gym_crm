package com.epam.gymcrm.client.workload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadNotifierTest {

  @InjectMocks private TrainerWorkloadNotifier trainerWorkloadNotifier;

  @Mock private TrainerWorkloadMessagePublisher trainerWorkloadMessagePublisher;

  @Test
  void notifyTrainerWorkloadShouldPublishMessage() {
    TrainerWorkloadRequest request = trainerWorkloadRequest();

    TrainerWorkloadNotificationResult result =
        trainerWorkloadNotifier.notifyTrainerWorkload(request);

    assertThat(result.successful()).isTrue();
    assertThat(result.errorMessage()).isNull();
    verify(trainerWorkloadMessagePublisher).publish(request);
  }

  @Test
  void notifyTrainerWorkloadShouldReturnFailureWhenPublishFails() {
    TrainerWorkloadRequest request = trainerWorkloadRequest();
    doThrow(new IllegalStateException("ActiveMQ broker is down"))
        .when(trainerWorkloadMessagePublisher)
        .publish(request);

    TrainerWorkloadNotificationResult result =
        trainerWorkloadNotifier.notifyTrainerWorkload(request);

    assertThat(result)
        .satisfies(
            notificationResult -> {
              assertThat(notificationResult.successful()).isFalse();
              assertThat(notificationResult.errorMessage())
                  .isEqualTo("ActiveMQ broker is down");
            });
    verify(trainerWorkloadMessagePublisher).publish(request);
  }

  private static TrainerWorkloadRequest trainerWorkloadRequest() {
    return new TrainerWorkloadRequest(
        10L,
        "Training.Trainer",
        "Training",
        "Trainer",
        true,
        LocalDate.of(2026, Month.MAY, 3),
        60,
        TrainerWorkloadActionType.ADD);
  }
}
