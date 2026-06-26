package com.epam.gymcrm.workload.exception;

public class TrainerWorkloadNotFoundException extends RuntimeException {

  public TrainerWorkloadNotFoundException(String username) {
    super("Trainer workload not found: " + username);
  }
}
