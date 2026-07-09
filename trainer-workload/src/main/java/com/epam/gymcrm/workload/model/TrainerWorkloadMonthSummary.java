package com.epam.gymcrm.workload.model;

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
public class TrainerWorkloadMonthSummary {

  private int month;

  private int trainingsSummaryDuration;
}
