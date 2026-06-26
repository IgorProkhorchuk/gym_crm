package com.epam.gymcrm.client.workload;

import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadNotifier implements TrainerWorkloadNotificationService {

  private static final String CIRCUIT_BREAKER_NAME = "trainerWorkload";

  private final TrainerWorkloadClient trainerWorkloadClient;
  private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

  @Override
  public TrainerWorkloadNotificationResult notifyTrainerWorkload(TrainerWorkloadRequest request) {
    CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_NAME);
    return circuitBreaker.run(updateRequest(request), logFailedUpdate(request));
  }

  private Supplier<TrainerWorkloadNotificationResult> updateRequest(TrainerWorkloadRequest request) {
    return () -> {
      trainerWorkloadClient.updateTrainerWorkload(request);
      return new TrainerWorkloadNotificationResult(true, null);
    };
  }

  private Function<Throwable, TrainerWorkloadNotificationResult> logFailedUpdate(
      TrainerWorkloadRequest request) {
    return failure -> {
      log.warn(
          "Trainer workload update failed, trainingId={}, actionType={}, trainingDate={}, "
              + "trainingDuration={}",
          request.trainingId(),
          request.actionType(),
          request.trainingDate(),
          request.trainingDuration(),
          failure);
      return new TrainerWorkloadNotificationResult(false, failure.getMessage());
    };
  }
}
