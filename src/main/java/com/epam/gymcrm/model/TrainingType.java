package com.epam.gymcrm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TrainingType {
    private Long trainingTypeId;
    private String trainingTypeName;
}
