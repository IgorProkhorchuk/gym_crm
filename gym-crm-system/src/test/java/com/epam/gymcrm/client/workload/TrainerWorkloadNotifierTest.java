package com.epam.gymcrm.client.workload;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadNotifierTest {

  @InjectMocks private TrainerWorkloadNotifier trainerWorkloadNotifier;

  @Mock private TrainerWorkloadClient trainerWorkloadClient;

  @Mock private CircuitBreakerFactory<?, ?> circuitBreakerFactory;

  @Mock private CircuitBreaker circuitBreaker;

  @Test
  void notifyTrainerWorkloadShouldSendRequestThroughCircuitBreaker() {
    TrainerWorkloadRequest request = trainerWorkloadRequest();
    when(circuitBreakerFactory.create("trainerWorkload")).thenReturn(circuitBreaker);
    runCircuitBreakerInvocation();

    trainerWorkloadNotifier.notifyTrainerWorkload(request);

    verify(trainerWorkloadClient).updateTrainerWorkload(request);
  }

  @Test
  void notifyTrainerWorkloadShouldLogFailureWithoutThrowingException() {
    TrainerWorkloadRequest request = trainerWorkloadRequest();
    when(circuitBreakerFactory.create("trainerWorkload")).thenReturn(circuitBreaker);
    runCircuitBreakerInvocation();
    doThrow(new IllegalStateException("Trainer workload service is down"))
        .when(trainerWorkloadClient)
        .updateTrainerWorkload(request);

    assertThatCode(() -> trainerWorkloadNotifier.notifyTrainerWorkload(request))
        .doesNotThrowAnyException();

    verify(trainerWorkloadClient).updateTrainerWorkload(request);
  }

  private void runCircuitBreakerInvocation() {
    when(circuitBreaker.run(any(), any()))
        .thenAnswer(
            invocation -> {
              Supplier<Void> supplier = invocation.getArgument(0);
              Function<Throwable, Void> fallback = invocation.getArgument(1);
              try {
                return supplier.get();
              } catch (RuntimeException exception) {
                return fallback.apply(exception);
              }
            });
  }

  private static TrainerWorkloadRequest trainerWorkloadRequest() {
    return new TrainerWorkloadRequest(
        "Training.Trainer",
        "Training",
        "Trainer",
        true,
        LocalDate.of(2026, 5, 3),
        60,
        TrainerWorkloadActionType.ADD);
  }
}
