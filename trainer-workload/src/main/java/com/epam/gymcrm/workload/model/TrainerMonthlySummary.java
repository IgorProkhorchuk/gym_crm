package com.epam.gymcrm.workload.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@Table(name = "trainer_monthly_summaries")
public class TrainerMonthlySummary {

  @Id
  @ToString.Include
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trainer_username", nullable = false, referencedColumnName = "username")
  private TrainerWorkload trainer;

  @Column(name = "training_year", nullable = false)
  private int trainingYear;

  @Column(name = "training_month", nullable = false)
  private int trainingMonth;

  @Column(name = "summary_duration", nullable = false)
  private int summaryDuration;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;
}
