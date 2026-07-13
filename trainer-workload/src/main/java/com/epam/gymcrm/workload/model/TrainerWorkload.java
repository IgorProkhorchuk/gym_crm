package com.epam.gymcrm.workload.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Document(collection = "trainer_workloads")
@CompoundIndex(
    name = "idx_trainer_workloads_first_name_last_name",
    def = "{'firstName': 1, 'lastName': 1}"
)
public class TrainerWorkload {

  @Id
  @EqualsAndHashCode.Include
  @ToString.Include
  private String username;

  private String firstName;

  private String lastName;

  private boolean active;

  @Builder.Default
  private List<TrainerWorkloadYearSummary> years = new ArrayList<>();
}
