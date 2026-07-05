package com.epam.gymcrm.workload.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.epam.gymcrm.workload.config.MessagingProperties;
import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import com.epam.gymcrm.workload.web.logging.RestLoggingInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadMessageListenerTest {

  private final ObjectMapper objectMapper =
      new ObjectMapper()
          .findAndRegisterModules()
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  private final MessagingProperties messagingProperties =
      new MessagingProperties(
          "trainer.workload.events",
          "trainer.workload.events.dlq",
          new MessagingProperties.Listener("1"),
          new MessagingProperties.Redelivery(3, 1000L, true, 2.0));

  @Mock private TrainerWorkloadService trainerWorkloadService;
  @Mock private JmsTemplate jmsTemplate;

  private TrainerWorkloadMessageListener listener;

  @BeforeEach
  void setUp() {
    listener =
        new TrainerWorkloadMessageListener(
            objectMapper,
            trainerWorkloadService,
            validator,
            jmsTemplate,
            messagingProperties);
    MDC.clear();
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void handleMessageShouldDeserializePayloadAndUpdateTrainerWorkload() {
    doAnswer(
            invocation -> {
              assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID))
                  .isEqualTo("request-transaction-id");
              return null;
            })
        .when(trainerWorkloadService)
        .updateTrainerWorkload(any(TrainerWorkloadRequest.class));

    listener.handleMessage(validPayload(), "request-transaction-id", 1);

    ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
        ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
    verify(trainerWorkloadService).updateTrainerWorkload(requestCaptor.capture());
    assertThat(requestCaptor.getValue())
        .isEqualTo(
            new TrainerWorkloadRequest(
                10L,
                "Training.Trainer",
                "Training",
                "Trainer",
                true,
                LocalDate.of(2026, Month.MAY, 3),
                60,
                ActionType.ADD));
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNull();
  }

  @Test
  void handleMessageShouldThrowIllegalArgumentExceptionWhenPayloadIsNotJson() {
    assertThatThrownBy(() -> listener.handleMessage("{not-json", "request-transaction-id", 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Failed to deserialize trainer workload message")
        .hasCauseInstanceOf(JsonProcessingException.class);

    verifyNoInteractions(trainerWorkloadService);
    verifyNoInteractions(jmsTemplate);
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNull();
  }

  @Test
  void handleMessageShouldThrowConstraintViolationExceptionWhenPayloadIsInvalid() {
    assertThatThrownBy(() -> listener.handleMessage(invalidPayload(), "request-transaction-id", 1))
        .isInstanceOf(ConstraintViolationException.class);

    verifyNoInteractions(trainerWorkloadService);
    verifyNoInteractions(jmsTemplate);
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNull();
  }

  @Test
  void handleMessageShouldGenerateTransactionIdWhenMessageHeaderIsMissing() {
    doAnswer(
            invocation -> {
              String transactionId = MDC.get(RestLoggingInterceptor.TRANSACTION_ID);
              assertThat(transactionId).isNotBlank();
              assertThat(UUID.fromString(transactionId)).isNotNull();
              return null;
            })
        .when(trainerWorkloadService)
        .updateTrainerWorkload(any(TrainerWorkloadRequest.class));

    listener.handleMessage(validPayload(), null, 1);

    verify(trainerWorkloadService).updateTrainerWorkload(any(TrainerWorkloadRequest.class));
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNull();
  }

  @Test
  void handleMessageShouldMoveInvalidPayloadToDeadLetterQueueOnFinalDelivery() {
    listener.handleMessage(invalidPayload(), "request-transaction-id", 4);

    verifyNoInteractions(trainerWorkloadService);
    verify(jmsTemplate)
        .convertAndSend(
            eq("trainer.workload.events.dlq"),
            eq(invalidPayload()),
            any(MessagePostProcessor.class));
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNull();
  }

  @Test
  void handleMessageShouldMoveMalformedJsonToDeadLetterQueueOnFinalDelivery() {
    listener.handleMessage("{not-json", "request-transaction-id", 4);

    verifyNoInteractions(trainerWorkloadService);
    verify(jmsTemplate)
        .convertAndSend(
            eq("trainer.workload.events.dlq"),
            eq("{not-json"),
            any(MessagePostProcessor.class));
    verifyNoMoreInteractions(jmsTemplate);
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNull();
  }

  private static String validPayload() {
    return
        """
        {
          "trainingId": 10,
          "trainerUsername": "Training.Trainer",
          "trainerFirstName": "Training",
          "trainerLastName": "Trainer",
          "trainerStatus": true,
          "trainingDate": "2026-05-03",
          "trainingDuration": 60,
          "actionType": "ADD"
        }
        """;
  }

  private static String invalidPayload() {
    return
        """
        {
          "trainingId": 10,
          "trainerUsername": "",
          "trainerFirstName": "Training",
          "trainerLastName": "Trainer",
          "trainerStatus": true,
          "trainingDate": "2026-05-03",
          "trainingDuration": 0,
          "actionType": "ADD"
        }
        """;
  }
}
