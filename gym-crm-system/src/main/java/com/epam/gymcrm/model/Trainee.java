package com.epam.gymcrm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "trainees")
public class Trainee {

  @Id
  @EqualsAndHashCode.Include
  @ToString.Include
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
  @JoinColumn(
      name = "user_id",
      nullable = false,
      unique = true,
      foreignKey = @ForeignKey(name = "fk_trainees_user"))
  private User user;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(name = "address")
  private String address;

  @Builder.Default
  @ManyToMany
  @JoinTable(
      name = "trainees_trainers",
      joinColumns =
          @JoinColumn(
              name = "trainee_id",
              nullable = false,
              foreignKey = @ForeignKey(name = "fk_trainees_trainers_trainee")),
      inverseJoinColumns =
          @JoinColumn(
              name = "trainer_id",
              nullable = false,
              foreignKey = @ForeignKey(name = "fk_trainees_trainers_trainer")))
  private Set<Trainer> trainers = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private Set<Training> trainings = new HashSet<>();
}
