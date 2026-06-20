package com.epam.gymcrm.workload.exception;

/**
 * Signals that trainer workload record does not exist.
 */
public class TrainerWorkloadNotFoundException extends RuntimeException {

  public TrainerWorkloadNotFoundException(String username) {
    super("Trainer workload not found: " + username);
  }
}
