package com.epam.gymcrm.client.workload;

import com.epam.gymcrm.web.logging.RestLoggingInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessagePublisher {

  private static final String TRANSACTION_ID_PROPERTY = "transactionId";
  private static final String TRAINING_ID_PROPERTY = "trainingId";
  private static final String ACTION_TYPE_PROPERTY = "actionType";
  private final JmsTemplate jmsTemplate;
  private final ObjectMapper objectMapper;

  @Value("${trainer-workload.messaging.queue}")
  private String destination;

  private static String resolveTransactionId() {
    String transactionId = MDC.get(RestLoggingInterceptor.TRANSACTION_ID);
    if (transactionId == null || transactionId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return transactionId;
  }

  public void publish(TrainerWorkloadRequest request) {
    try {
      String payload = objectMapper.writeValueAsString(request);
      jmsTemplate.convertAndSend(
          destination,
          payload,
          message -> {
            message.setStringProperty(TRANSACTION_ID_PROPERTY, resolveTransactionId());
            message.setLongProperty(TRAINING_ID_PROPERTY, request.trainingId());
            message.setStringProperty(ACTION_TYPE_PROPERTY, request.actionType().name());
            return message;
          });
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize trainer workload message", exception);
    }
  }
}
