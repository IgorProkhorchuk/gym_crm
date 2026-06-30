package com.epam.gymcrm.workload.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "trainer_workloads")
public class TrainerWorkload {

  @Id
  @EqualsAndHashCode.Include
  @ToString.Include
  @Column(name = "username", nullable = false, length = 100)
  private String username;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "active", nullable = false)
  private boolean active;

  @Builder.Default
  @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TrainerMonthlySummary> monthlySummaries = new ArrayList<>();
}
