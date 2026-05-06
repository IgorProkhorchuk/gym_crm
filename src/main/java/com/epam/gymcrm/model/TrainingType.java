package com.epam.gymcrm.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "training_type")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class TrainingType {

    @JsonAlias("id")
    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainingTypeId;

    @Column(nullable = false, unique = true)
    private String trainingTypeName;
}
