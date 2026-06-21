package com.epam.gymcrm.client.workload;

import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

/**
 * Notifies trainer workload service about training changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadNotifier {

  private static final String CIRCUIT_BREAKER_NAME = "trainerWorkload";

  private final TrainerWorkloadClient trainerWorkloadClient;
  private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

  /**
   * Sends trainer workload update without failing the main training flow.
   *
   * @param request trainer workload update request
   */
  public void notifyTrainerWorkload(TrainerWorkloadRequest request) {
    CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_NAME);
    circuitBreaker.run(updateRequest(request), logFailedUpdate(request));
  }

  private Supplier<Void> updateRequest(TrainerWorkloadRequest request) {
    return () -> {
      trainerWorkloadClient.updateTrainerWorkload(request);
      return null;
    };
  }

  private Function<Throwable, Void> logFailedUpdate(TrainerWorkloadRequest request) {
    return failure -> {
      log.warn(
          "Trainer workload update failed, actionType={}, trainerUsername={}, trainingDate={}, "
              + "trainingDuration={}",
          request.actionType(),
          request.trainerUsername(),
          request.trainingDate(),
          request.trainingDuration(),
          failure);
      return null;
    };
  }
}
