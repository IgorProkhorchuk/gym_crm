package com.epam.gymcrm.client.workload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.web.logging.RestLoggingInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

class TrainerWorkloadMessagePublisherTest {

  private static final String DESTINATION = "trainer.workload.events";

  private final JmsTemplate jmsTemplate = mock(JmsTemplate.class);
  private final ObjectMapper objectMapper =
      new ObjectMapper()
          .findAndRegisterModules()
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private TrainerWorkloadMessagePublisher publisher;

  @BeforeEach
  void setUp() {
    publisher = new TrainerWorkloadMessagePublisher(jmsTemplate, objectMapper);
    ReflectionTestUtils.setField(publisher, "destination", DESTINATION);
    MDC.clear();
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void publishShouldSendSerializedTrainerWorkloadRequestWithMessageProperties()
      throws JMSException {
    TrainerWorkloadRequest request = trainerWorkloadRequest();
    MDC.put(RestLoggingInterceptor.TRANSACTION_ID, "request-transaction-id");

    publisher.publish(request);

    ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<MessagePostProcessor> postProcessorCaptor =
        ArgumentCaptor.forClass(MessagePostProcessor.class);
    verify(jmsTemplate)
        .convertAndSend(eq(DESTINATION), payloadCaptor.capture(), postProcessorCaptor.capture());
    assertThat(payloadCaptor.getValue())
        .contains("\"trainingId\":10")
        .contains("\"trainerUsername\":\"Training.Trainer\"")
        .contains("\"trainingDate\":\"2026-05-03\"")
        .contains("\"trainingDuration\":60")
        .contains("\"actionType\":\"ADD\"");

    Message message = mock(Message.class);
    postProcessorCaptor.getValue().postProcessMessage(message);
    verify(message).setStringProperty("transactionId", "request-transaction-id");
    verify(message).setLongProperty("trainingId", 10L);
    verify(message).setStringProperty("actionType", "ADD");
  }

  @Test
  void publishShouldThrowIllegalStateExceptionWhenSerializationFails() throws Exception {
    ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
    TrainerWorkloadMessagePublisher failingPublisher =
        new TrainerWorkloadMessagePublisher(jmsTemplate, failingObjectMapper);
    ReflectionTestUtils.setField(failingPublisher, "destination", DESTINATION);
    TrainerWorkloadRequest request = trainerWorkloadRequest();
    when(failingObjectMapper.writeValueAsString(request))
        .thenThrow(new JsonProcessingException("Cannot serialize request") {});

    assertThatThrownBy(() -> failingPublisher.publish(request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to serialize trainer workload message")
        .hasCauseInstanceOf(JsonProcessingException.class);

    verify(jmsTemplate, never())
        .convertAndSend(anyString(), anyString(), any(MessagePostProcessor.class));
  }

  private static TrainerWorkloadRequest trainerWorkloadRequest() {
    return new TrainerWorkloadRequest(
        10L,
        "Training.Trainer",
        "Training",
        "Trainer",
        true,
        LocalDate.of(2026, Month.MAY, 3),
        60,
        TrainerWorkloadActionType.ADD);
  }
}
