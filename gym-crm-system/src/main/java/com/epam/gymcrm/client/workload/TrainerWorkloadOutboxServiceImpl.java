package com.epam.gymcrm.client.workload;

import com.epam.gymcrm.model.TrainerWorkloadOutboxEvent;
import com.epam.gymcrm.model.TrainerWorkloadOutboxStatus;
import com.epam.gymcrm.repository.TrainerWorkloadOutboxRepository;
import com.epam.gymcrm.web.logging.RestLoggingInterceptor;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadOutboxServiceImpl implements TrainerWorkloadOutboxService {

  private static final int DISPATCH_BATCH_SIZE = 20;
  private static final Duration RETRY_DELAY = Duration.ofSeconds(30);

  private final TrainerWorkloadOutboxRepository trainerWorkloadOutboxRepository;
  private final TrainerWorkloadNotificationService trainerWorkloadNotificationService;

  @Override
  @Transactional
  public void savePendingEvent(Long trainingId, TrainerWorkloadRequest request) {
    trainerWorkloadOutboxRepository.save(
        TrainerWorkloadOutboxEvent.pending(
            trainingId, resolveTransactionId(), request, Instant.now()));
  }

  @Override
  @Transactional
  public void dispatchPendingEvents() {
    Instant now = Instant.now();
    List<TrainerWorkloadOutboxEvent> events =
        trainerWorkloadOutboxRepository
            .findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                TrainerWorkloadOutboxStatus.PENDING, now, PageRequest.of(0, DISPATCH_BATCH_SIZE));

    events.forEach(event -> dispatchEvent(event, now));
  }

  private void dispatchEvent(TrainerWorkloadOutboxEvent event, Instant now) {
    String previousTransactionId = MDC.get(RestLoggingInterceptor.TRANSACTION_ID);
    MDC.put(RestLoggingInterceptor.TRANSACTION_ID, event.getTransactionId());

    try {
      log.info(
          "Dispatching trainer workload outbox event, outboxEventId={}, trainingId={}, actionType={}",
          event.getId(),
          event.getTrainingId(),
          event.getActionType());
      TrainerWorkloadNotificationResult result =
          trainerWorkloadNotificationService.notifyTrainerWorkload(
              event.toTrainerWorkloadRequest());

      if (result.successful()) {
        event.markSent(now);
        log.info("Trainer workload outbox event sent, outboxEventId={}", event.getId());
        return;
      }

      event.markRetry(now.plus(RETRY_DELAY), result.errorMessage());
      log.info(
          "Trainer workload outbox event scheduled for retry, outboxEventId={}, retryCount={}",
          event.getId(),
          event.getRetryCount());
    } finally {
      restoreTransactionId(previousTransactionId);
    }
  }

  private static String resolveTransactionId() {
    String transactionId = MDC.get(RestLoggingInterceptor.TRANSACTION_ID);
    if (transactionId == null || transactionId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return transactionId;
  }

  private static void restoreTransactionId(String previousTransactionId) {
    if (previousTransactionId == null) {
      MDC.remove(RestLoggingInterceptor.TRANSACTION_ID);
      return;
    }
    MDC.put(RestLoggingInterceptor.TRANSACTION_ID, previousTransactionId);
  }
}
