package com.epam.gymcrm.model;

import com.epam.gymcrm.client.workload.TrainerWorkloadActionType;
import com.epam.gymcrm.client.workload.TrainerWorkloadRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "trainer_workload_outbox")
public class TrainerWorkloadOutboxEvent {

  private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include
  @Column(name = "id")
  private Long id;

  @Column(name = "training_id", nullable = false)
  private Long trainingId;

  @Column(name = "trainer_username", nullable = false, length = 100)
  private String trainerUsername;

  @Column(name = "trainer_first_name", nullable = false, length = 100)
  private String trainerFirstName;

  @Column(name = "trainer_last_name", nullable = false, length = 100)
  private String trainerLastName;

  @Column(name = "trainer_status", nullable = false)
  private Boolean trainerStatus;

  @Column(name = "training_date", nullable = false)
  private LocalDate trainingDate;

  @Column(name = "training_duration", nullable = false)
  private Integer trainingDuration;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false, length = 20)
  private TrainerWorkloadActionType actionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private TrainerWorkloadOutboxStatus status;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Column(name = "next_retry_at", nullable = false)
  private Instant nextRetryAt;

  @Column(name = "error_message", length = MAX_ERROR_MESSAGE_LENGTH)
  private String errorMessage;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /**
   * Creates pending outbox event from trainer workload request.
   *
   * @param trainingId source training id
   * @param request trainer workload update request
   * @param now current time
   * @return pending outbox event
   */
  public static TrainerWorkloadOutboxEvent pending(
      Long trainingId, TrainerWorkloadRequest request, Instant now) {
    return TrainerWorkloadOutboxEvent.builder()
        .trainingId(trainingId)
        .trainerUsername(request.trainerUsername())
        .trainerFirstName(request.trainerFirstName())
        .trainerLastName(request.trainerLastName())
        .trainerStatus(request.trainerStatus())
        .trainingDate(request.trainingDate())
        .trainingDuration(request.trainingDuration())
        .actionType(request.actionType())
        .status(TrainerWorkloadOutboxStatus.PENDING)
        .retryCount(0)
        .nextRetryAt(now)
        .build();
  }

  /**
   * Converts outbox event into trainer workload request.
   *
   * @return trainer workload update request
   */
  public TrainerWorkloadRequest toTrainerWorkloadRequest() {
    return new TrainerWorkloadRequest(
        trainingId,
        trainerUsername,
        trainerFirstName,
        trainerLastName,
        trainerStatus,
        trainingDate,
        trainingDuration,
        actionType);
  }

  /**
   * Marks event as successfully delivered.
   *
   * @param now current time
   */
  public void markSent(Instant now) {
    status = TrainerWorkloadOutboxStatus.SENT;
    nextRetryAt = now;
    errorMessage = null;
  }

  /**
   * Keeps event pending and schedules another delivery attempt.
   *
   * @param nextAttemptAt next delivery attempt time
   * @param failureMessage delivery failure message
   */
  public void markRetry(Instant nextAttemptAt, String failureMessage) {
    status = TrainerWorkloadOutboxStatus.PENDING;
    retryCount++;
    nextRetryAt = nextAttemptAt;
    errorMessage = truncate(failureMessage);
  }

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  private static String truncate(String value) {
    if (value == null || value.length() <= MAX_ERROR_MESSAGE_LENGTH) {
      return value;
    }
    return value.substring(0, MAX_ERROR_MESSAGE_LENGTH);
  }
}
