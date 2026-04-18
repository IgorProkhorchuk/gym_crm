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
        if (entityClass == Trainee.class) return (Map<Long, T>) trainees;
        if (entityClass == Trainer.class) return (Map<Long, T>) trainers;
        if (entityClass == Training.class) return (Map<Long, T>) trainings;
        if (entityClass == TrainingType.class) return (Map<Long, T>) trainingTypes;
        throw new IllegalArgumentException("Unknown entity type: " + entityClass.getName());
    }
}