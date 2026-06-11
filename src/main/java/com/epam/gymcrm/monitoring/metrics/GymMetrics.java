package com.epam.gymcrm.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

  private static final String LOGIN_FAILED_METRIC = "gym.auth.login.failed";
  private static final String TRAINING_CREATION_SUCCEEDED_METRIC =
      "gym.training.creation.succeeded";
  private static final String TRAINING_CREATION_FAILED_METRIC = "gym.training.creation.failed";
  private static final String REASON_TAG = "reason";
  private static final String INVALID_CREDENTIALS = "invalid_credentials";
  private static final String AUTH_FAILED = "auth_failed";
  private static final String TRAINER_NOT_FOUND = "trainer_not_found";
  private static final String TRAINING_TYPE_NOT_FOUND = "training_type_not_found";

  private final Counter loginFailedInvalidCredentials;
  private final Counter trainingCreationSucceeded;
  private final Counter trainingCreationAuthFailed;
  private final Counter trainingCreationTrainerNotFound;
  private final Counter trainingCreationTrainingTypeNotFound;

  public GymMetrics(MeterRegistry meterRegistry) {
    this.loginFailedInvalidCredentials =
        Counter.builder(LOGIN_FAILED_METRIC)
            .tag(REASON_TAG, INVALID_CREDENTIALS)
            .register(meterRegistry);
    this.trainingCreationSucceeded =
        Counter.builder(TRAINING_CREATION_SUCCEEDED_METRIC).register(meterRegistry);
    this.trainingCreationAuthFailed =
        Counter.builder(TRAINING_CREATION_FAILED_METRIC)
            .tag(REASON_TAG, AUTH_FAILED)
            .register(meterRegistry);
    this.trainingCreationTrainerNotFound =
        Counter.builder(TRAINING_CREATION_FAILED_METRIC)
            .tag(REASON_TAG, TRAINER_NOT_FOUND)
            .register(meterRegistry);
    this.trainingCreationTrainingTypeNotFound =
        Counter.builder(TRAINING_CREATION_FAILED_METRIC)
            .tag(REASON_TAG, TRAINING_TYPE_NOT_FOUND)
            .register(meterRegistry);
  }

  public void recordLoginFailedInvalidCredentials() {
    loginFailedInvalidCredentials.increment();
  }

  public void recordTrainingCreationSucceeded() {
    trainingCreationSucceeded.increment();
  }

  public void recordTrainingCreationAuthFailed() {
    trainingCreationAuthFailed.increment();
  }

  public void recordTrainingCreationTrainerNotFound() {
    trainingCreationTrainerNotFound.increment();
  }

  public void recordTrainingCreationTrainingTypeNotFound() {
    trainingCreationTrainingTypeNotFound.increment();
  }
}
