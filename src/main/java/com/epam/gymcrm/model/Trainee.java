package com.epam.gymcrm.model;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "trainee")
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
            foreignKey = @ForeignKey(name = "fk_trainee_user")
    )
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(
                    name = "trainee_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "fk_trainee_trainer_trainee")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "trainer_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "fk_trainee_trainer_trainer")
            )
    )
    private Set<Trainer> trainers = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Training> trainings = new HashSet<>();
}
