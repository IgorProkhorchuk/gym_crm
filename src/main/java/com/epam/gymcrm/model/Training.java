package com.epam.gymcrm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Training {
    private Long traineeId;
    private Long trainerId;
    private Long trainingId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private Duration trainingDuration;
}
