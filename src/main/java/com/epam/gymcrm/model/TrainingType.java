package com.epam.gymcrm.model;

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

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainingTypeId;

    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;
}
