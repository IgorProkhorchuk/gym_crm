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
  private final TrainerWorkloadMessagePublisher trainerWorkloadMessagePublisher;

  @Override
  public TrainerWorkloadNotificationResult notifyTrainerWorkload(TrainerWorkloadRequest request) {
    try {
      trainerWorkloadMessagePublisher.publish(request);
      return new TrainerWorkloadNotificationResult(true, null);
    } catch (RuntimeException exception) {
      log.warn(
          "Trainer workload message publish failed, trainingId={}, actionType={}, trainingDate={}, "
          + "trainingDuration={}",
          request.trainingId(),
          request.actionType(),
          request.trainingDate(),
          request.trainingDuration(),
          exception);
      return new TrainerWorkloadNotificationResult(false, exception.getMessage());
    }
  }
}
