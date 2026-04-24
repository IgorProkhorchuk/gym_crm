package com.epam.gymcrm.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TrainingType {
    @JsonAlias("id")
    private Long trainingTypeId;
    private String trainingTypeName;
}
