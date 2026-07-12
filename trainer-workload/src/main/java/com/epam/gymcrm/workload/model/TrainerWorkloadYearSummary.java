package com.epam.gymcrm.workload.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerWorkloadYearSummary {

  private int year;

  @Builder.Default
  private List<TrainerWorkloadMonthSummary> months = new ArrayList<>();
}
