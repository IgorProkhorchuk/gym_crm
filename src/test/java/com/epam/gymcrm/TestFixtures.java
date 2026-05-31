package com.epam.gymcrm;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import java.time.LocalDate;

public final class TestFixtures {

  private TestFixtures() {}

  public static User user(String firstName, String lastName, String username) {
    return User.builder()
        .firstName(firstName)
        .lastName(lastName)
        .username(username)
        .password("password")
        .active(true)
        .build();
  }

  public static Trainee trainee(String firstName, String lastName, String username) {
    return Trainee.builder()
        .user(user(firstName, lastName, username))
        .dateOfBirth(LocalDate.of(1995, 1, 10))
        .address("Main Street, 123")
        .build();
  }

  public static Trainee trainee(Long id, String firstName, String lastName, String username) {
    return Trainee.builder()
        .id(id)
        .user(user(firstName, lastName, username))
        .dateOfBirth(LocalDate.of(1995, 1, 10))
        .address("Main Street, 123")
        .build();
  }

  public static Trainer trainer(String firstName, String lastName, String username) {
    return trainer(firstName, lastName, username, trainingType("Fitness"));
  }

  public static Trainer trainer(
      String firstName, String lastName, String username, TrainingType specialization) {
    return Trainer.builder()
        .user(user(firstName, lastName, username))
        .specialization(specialization)
        .build();
  }

  public static Trainer trainer(Long id, String firstName, String lastName, String username) {
    return trainer(id, firstName, lastName, username, trainingType("Fitness"));
  }

  public static Trainer trainer(
      Long id, String firstName, String lastName, String username, TrainingType specialization) {
    return Trainer.builder()
        .id(id)
        .user(user(firstName, lastName, username))
        .specialization(specialization)
        .build();
  }

  public static TrainingType trainingType(String name) {
    return TrainingType.builder().trainingTypeName(name).build();
  }

  public static Training training(Trainee trainee, Trainer trainer, TrainingType trainingType) {
    return Training.builder()
        .trainee(trainee)
        .trainer(trainer)
        .trainingType(trainingType)
        .trainingName("Yoga Basics")
        .trainingDate(LocalDate.of(2026, 5, 3))
        .trainingDuration(60)
        .build();
  }
}
