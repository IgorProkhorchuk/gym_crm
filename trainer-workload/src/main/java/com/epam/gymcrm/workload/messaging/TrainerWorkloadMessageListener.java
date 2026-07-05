package com.epam.gymcrm.workload.messaging;

import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageListener {

  private final ObjectMapper objectMapper;
  private final TrainerWorkloadService trainerWorkloadService;
  private final Validator validator;

  @JmsListener(destination = "${trainer-workload.messaging.queue}")
  public void handleMessage(String payload) {
    try {
      TrainerWorkloadRequest request =
          objectMapper.readValue(payload, TrainerWorkloadRequest.class);

      validate(request);
      trainerWorkloadService.updateTrainerWorkload(request);
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("Failed to deserialize trainer workload message", exception);
    }
  }

  private void validate(TrainerWorkloadRequest request) {
    Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
