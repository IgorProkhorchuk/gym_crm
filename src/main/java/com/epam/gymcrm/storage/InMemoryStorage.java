package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class InMemoryStorage {

    private Map<Long, Trainee> trainees;
    private Map<Long, Trainer> trainers;
    private Map<Long, Training> trainings;
    private Map<Long, TrainingType> trainingTypes;

    @Autowired
    public void setTrainees(Map<Long, Trainee> trainees) { this.trainees = trainees; }

    @Autowired
    public void setTrainers(Map<Long, Trainer> trainers) { this.trainers = trainers; }

    @Autowired
    public void setTrainings(Map<Long, Training> trainings) { this.trainings = trainings; }

    @Autowired
    public void setTrainingTypes(Map<Long, TrainingType> trainingTypes) { this.trainingTypes = trainingTypes; }

    @SuppressWarnings("unchecked")
    public <T> Map<Long, T> getStorage(Class<T> entityClass) {
        return switch (entityClass) {
            case null -> throw new IllegalArgumentException("Entity class must not be null");

            case Class<?> entityType when entityType == Trainee.class -> (Map<Long, T>) trainees;
            case Class<?> entityType when entityType == Trainer.class -> (Map<Long, T>) trainers;
            case Class<?> entityType when entityType == Training.class -> (Map<Long, T>) trainings;
            case Class<?> entityType when entityType == TrainingType.class -> (Map<Long, T>) trainingTypes;

            default -> throw new IllegalArgumentException(
                    "Unknown entity type: " + entityClass.getName()
            );
        };
    }
}
