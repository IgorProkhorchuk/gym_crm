package com.epam.gymcrm.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Training {
    private Long traineeId;
    private Long trainerId;
    @JsonAlias("id")
    private Long trainingId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private Duration trainingDuration;
}
