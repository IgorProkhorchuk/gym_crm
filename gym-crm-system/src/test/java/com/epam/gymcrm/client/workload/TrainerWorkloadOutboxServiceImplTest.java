package com.epam.gymcrm.client.workload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.model.TrainerWorkloadOutboxEvent;
import com.epam.gymcrm.model.TrainerWorkloadOutboxStatus;
import com.epam.gymcrm.repository.TrainerWorkloadOutboxRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadOutboxServiceImplTest {

  @InjectMocks private TrainerWorkloadOutboxServiceImpl trainerWorkloadOutboxService;

  @Mock private TrainerWorkloadOutboxRepository trainerWorkloadOutboxRepository;

  @Mock private TrainerWorkloadNotificationService trainerWorkloadNotificationService;

  @Captor private ArgumentCaptor<TrainerWorkloadOutboxEvent> eventCaptor;

  @Test
  void savePendingEventShouldStorePendingOutboxEvent() {
    TrainerWorkloadRequest request = trainerWorkloadRequest(TrainerWorkloadActionType.ADD);

    trainerWorkloadOutboxService.savePendingEvent(10L, request);

    verify(trainerWorkloadOutboxRepository).save(eventCaptor.capture());
    TrainerWorkloadOutboxEvent event = eventCaptor.getValue();
    assertAll(
        () -> assertThat(event.getTrainingId()).isEqualTo(10L),
        () -> assertThat(event.getTrainerUsername()).isEqualTo("Training.Trainer"),
        () -> assertThat(event.getTrainerFirstName()).isEqualTo("Training"),
        () -> assertThat(event.getTrainerLastName()).isEqualTo("Trainer"),
        () -> assertThat(event.getTrainerStatus()).isTrue(),
        () -> assertThat(event.getTrainingDate()).isEqualTo(LocalDate.of(2026, 5, 3)),
        () -> assertThat(event.getTrainingDuration()).isEqualTo(60),
        () -> assertThat(event.getActionType()).isEqualTo(TrainerWorkloadActionType.ADD),
        () -> assertThat(event.getStatus()).isEqualTo(TrainerWorkloadOutboxStatus.PENDING),
        () -> assertThat(event.getRetryCount()).isZero(),
        () -> assertThat(event.getNextRetryAt()).isNotNull());
  }

  @Test
  void dispatchPendingEventsShouldMarkEventAsSentWhenNotificationSucceeds() {
    TrainerWorkloadOutboxEvent event = pendingEvent();
    when(trainerWorkloadOutboxRepository
            .findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                eq(TrainerWorkloadOutboxStatus.PENDING), any(Instant.class), any(Pageable.class)))
        .thenReturn(List.of(event));
    when(trainerWorkloadNotificationService.notifyTrainerWorkload(event.toTrainerWorkloadRequest()))
        .thenReturn(new TrainerWorkloadNotificationResult(true, null));

    trainerWorkloadOutboxService.dispatchPendingEvents();

    assertAll(
        () -> assertThat(event.getStatus()).isEqualTo(TrainerWorkloadOutboxStatus.SENT),
        () -> assertThat(event.getErrorMessage()).isNull(),
        () -> assertThat(event.getRetryCount()).isZero());
  }

  @Test
  void dispatchPendingEventsShouldScheduleRetryWhenNotificationFails() {
    TrainerWorkloadOutboxEvent event = pendingEvent();
    when(trainerWorkloadOutboxRepository
            .findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                eq(TrainerWorkloadOutboxStatus.PENDING), any(Instant.class), any(Pageable.class)))
        .thenReturn(List.of(event));
    when(trainerWorkloadNotificationService.notifyTrainerWorkload(event.toTrainerWorkloadRequest()))
        .thenReturn(new TrainerWorkloadNotificationResult(false, "Connection refused"));

    trainerWorkloadOutboxService.dispatchPendingEvents();

    assertAll(
        () -> assertThat(event.getStatus()).isEqualTo(TrainerWorkloadOutboxStatus.PENDING),
        () -> assertThat(event.getRetryCount()).isEqualTo(1),
        () -> assertThat(event.getErrorMessage()).isEqualTo("Connection refused"),
        () -> assertThat(event.getNextRetryAt()).isAfter(Instant.now()));
  }

  private static TrainerWorkloadOutboxEvent pendingEvent() {
    TrainerWorkloadOutboxEvent event =
        TrainerWorkloadOutboxEvent.pending(
            10L, trainerWorkloadRequest(TrainerWorkloadActionType.ADD), Instant.now());
    event.setId(1L);
    return event;
  }

  private static TrainerWorkloadRequest trainerWorkloadRequest(
      TrainerWorkloadActionType actionType) {
    return new TrainerWorkloadRequest(
        "Training.Trainer",
        "Training",
        "Trainer",
        true,
        LocalDate.of(2026, 5, 3),
        60,
        actionType);
  }
}
