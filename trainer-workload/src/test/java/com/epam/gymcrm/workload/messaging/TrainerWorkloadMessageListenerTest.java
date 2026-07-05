package com.epam.gymcrm.workload.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadMessageListenerTest {

  private final ObjectMapper objectMapper =
      new ObjectMapper()
          .findAndRegisterModules()
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Mock private TrainerWorkloadService trainerWorkloadService;

  private TrainerWorkloadMessageListener listener;

  @BeforeEach
  void setUp() {
    listener =
        new TrainerWorkloadMessageListener(objectMapper, trainerWorkloadService, validator);
  }

  @Test
  void handleMessageShouldDeserializePayloadAndUpdateTrainerWorkload() {
    listener.handleMessage(validPayload());

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
  }

  @Test
  void handleMessageShouldThrowIllegalArgumentExceptionWhenPayloadIsNotJson() {
    assertThatThrownBy(() -> listener.handleMessage("{not-json"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Failed to deserialize trainer workload message")
        .hasCauseInstanceOf(JsonProcessingException.class);

    verifyNoInteractions(trainerWorkloadService);
  }

  @Test
  void handleMessageShouldThrowConstraintViolationExceptionWhenPayloadIsInvalid() {
    assertThatThrownBy(() -> listener.handleMessage(invalidPayload()))
        .isInstanceOf(ConstraintViolationException.class);

    verifyNoInteractions(trainerWorkloadService);
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
