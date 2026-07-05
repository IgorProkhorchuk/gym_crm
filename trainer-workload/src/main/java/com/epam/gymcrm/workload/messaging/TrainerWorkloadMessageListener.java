package com.epam.gymcrm.workload.messaging;

import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import com.epam.gymcrm.workload.web.logging.RestLoggingInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageListener {

  private static final String TRANSACTION_ID_PROPERTY = "transactionId";

  private final ObjectMapper objectMapper;
  private final TrainerWorkloadService trainerWorkloadService;
  private final Validator validator;

  @JmsListener(destination = "${trainer-workload.messaging.queue}")
  public void handleMessage(
      String payload,
      @Header(name = TRANSACTION_ID_PROPERTY, required = false) String transactionId) {
    MDC.put(RestLoggingInterceptor.TRANSACTION_ID, resolveTransactionId(transactionId));
    try {
      TrainerWorkloadRequest request =
          objectMapper.readValue(payload, TrainerWorkloadRequest.class);

      validate(request);
      trainerWorkloadService.updateTrainerWorkload(request);
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("Failed to deserialize trainer workload message", exception);
    } finally {
      MDC.remove(RestLoggingInterceptor.TRANSACTION_ID);
    }
  }

  private static String resolveTransactionId(String transactionId) {
    if (transactionId == null || transactionId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return transactionId;
  }

  private void validate(TrainerWorkloadRequest request) {
    Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
