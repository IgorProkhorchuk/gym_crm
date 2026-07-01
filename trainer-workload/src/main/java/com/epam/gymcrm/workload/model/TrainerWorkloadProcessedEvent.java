package com.epam.gymcrm.workload.model;

import com.epam.gymcrm.workload.dto.ActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
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
@Table(
    name = "trainer_workload_processed_events",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_trainer_workload_processed_events_training_action",
            columnNames = {"training_id", "action_type"})
    })
public class TrainerWorkloadProcessedEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include
  @Column(name = "id")
  private Long id;

  @Column(name = "training_id", nullable = false)
  private Long trainingId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false, length = 20)
  private ActionType actionType;

  @Column(name = "processed_at", nullable = false)
  private Instant processedAt;

  public static TrainerWorkloadProcessedEvent fromRequest(
      Long trainingId,
      ActionType actionType,
      Instant processedAt
  ) {
    return TrainerWorkloadProcessedEvent.builder()
        .trainingId(trainingId)
        .actionType(actionType)
        .processedAt(processedAt)
        .build();
  }
}
