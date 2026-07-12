package com.epam.gymcrm.workload.model;

import com.epam.gymcrm.workload.dto.ActionType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Document(collection = "trainer_workload_processed_events")
@CompoundIndex(
    name = "uk_trainer_workload_processed_events_training_action",
    def = "{'trainingId': 1, 'actionType': 1}",
    unique = true
)
public class TrainerWorkloadProcessedEvent {

  @Id
  @ToString.Include
  private String id;

  private Long trainingId;

  private ActionType actionType;

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
