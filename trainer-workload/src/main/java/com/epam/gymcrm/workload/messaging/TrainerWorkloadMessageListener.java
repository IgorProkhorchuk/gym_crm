package com.epam.gymcrm.workload.messaging;

import com.epam.gymcrm.workload.config.MessagingProperties;
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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadMessageListener {

  private static final String TRANSACTION_ID_PROPERTY = "transactionId";
  private static final String DELIVERY_COUNT_HEADER = "JMSXDeliveryCount";
  private static final String ORIGINAL_QUEUE_PROPERTY = "originalQueue";
  private static final String ERROR_TYPE_PROPERTY = "errorType";
  private static final String ERROR_MESSAGE_PROPERTY = "errorMessage";

  private final ObjectMapper objectMapper;
  private final TrainerWorkloadService trainerWorkloadService;
  private final Validator validator;
  private final JmsTemplate jmsTemplate;
  private final MessagingProperties messagingProperties;

  @JmsListener(destination = "${trainer-workload.messaging.queue}")
  public void handleMessage(
      String payload,
      @Header(name = TRANSACTION_ID_PROPERTY, required = false) String transactionId,
      @Header(name = DELIVERY_COUNT_HEADER, required = false) Integer deliveryCount) {
    String resolvedTransactionId = resolveTransactionId(transactionId);
    MDC.put(RestLoggingInterceptor.TRANSACTION_ID, resolvedTransactionId);
    try {
      TrainerWorkloadRequest request =
          objectMapper.readValue(payload, TrainerWorkloadRequest.class);

      validate(request);
      trainerWorkloadService.updateTrainerWorkload(request);
    } catch (JsonProcessingException exception) {
      handleFailedMessage(
          payload,
          resolvedTransactionId,
          deliveryCount,
          new IllegalArgumentException(
              "Failed to deserialize trainer workload message", exception));
    } catch (RuntimeException exception) {
      handleFailedMessage(payload, resolvedTransactionId, deliveryCount, exception);
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

  private void handleFailedMessage(
      String payload,
      String transactionId,
      Integer deliveryCount,
      RuntimeException exception
  ) {
    if (!isFinalDelivery(deliveryCount)) {
      throw exception;
    }

    log.warn(
        "Trainer workload message moved to DLQ, destination={}, deliveryCount={}, errorType={}",
        messagingProperties.deadLetterQueue(),
        deliveryCount,
        exception.getClass().getSimpleName(),
        exception);
    jmsTemplate.convertAndSend(
        messagingProperties.deadLetterQueue(),
        payload,
        message -> {
          message.setStringProperty(TRANSACTION_ID_PROPERTY, transactionId);
          message.setStringProperty(ORIGINAL_QUEUE_PROPERTY, messagingProperties.queue());
          message.setStringProperty(ERROR_TYPE_PROPERTY, exception.getClass().getSimpleName());
          message.setStringProperty(ERROR_MESSAGE_PROPERTY, exception.getMessage());
          return message;
        });
  }

  private boolean isFinalDelivery(Integer deliveryCount) {
    return deliveryCount != null
        && deliveryCount > messagingProperties.redelivery().maximumRedeliveries();
  }
}
