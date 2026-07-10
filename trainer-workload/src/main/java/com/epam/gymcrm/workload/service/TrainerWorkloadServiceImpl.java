package com.epam.gymcrm.workload.service;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadMonthResponse;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.epam.gymcrm.workload.dto.TrainerWorkloadYearResponse;
import com.epam.gymcrm.workload.exception.TrainerWorkloadNotFoundException;
import com.epam.gymcrm.workload.model.TrainerWorkload;
import com.epam.gymcrm.workload.model.TrainerWorkloadMonthSummary;
import com.epam.gymcrm.workload.model.TrainerWorkloadProcessedEvent;
import com.epam.gymcrm.workload.model.TrainerWorkloadYearSummary;
import com.epam.gymcrm.workload.repository.TrainerWorkloadProcessedEventRepository;
import com.epam.gymcrm.workload.repository.TrainerWorkloadRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

  private final TrainerWorkloadRepository trainerWorkloadRepository;
  private final TrainerWorkloadProcessedEventRepository processedEventRepository;
  private final TransactionTemplate transactionTemplate;
  private final MeterRegistry meterRegistry;

  @Override
  public void updateTrainerWorkload(TrainerWorkloadRequest request) {
    log.info(
        "Updating trainer workload, trainingId={}, actionType={}, trainingDate={}, "
            + "trainingDuration={}",
        request.trainingId(),
        request.actionType(),
        request.trainingDate(),
        request.trainingDuration());

    try {
      transactionTemplate.executeWithoutResult(status -> processTrainerWorkloadEvent(request));
    } catch (DuplicateKeyException exception) {
      meterRegistry.counter(
          "trainer.workload.duplicate.events",
          "actionType",
          request.actionType().name()).increment();
      log.info(
          "Duplicate trainer workload event ignored, trainingId={}, actionType={}",
          request.trainingId(),
          request.actionType());
    }
  }

  private void processTrainerWorkloadEvent(TrainerWorkloadRequest request) {
    processedEventRepository.insert(TrainerWorkloadProcessedEvent.fromRequest(
        request.trainingId(),
        request.actionType(),
        Instant.now()));

    TrainerWorkload trainer = trainerWorkloadRepository.findById(request.trainerUsername())
        .orElseGet(() -> TrainerWorkload.builder()
            .username(request.trainerUsername())
            .build());

    trainer.setFirstName(request.trainerFirstName());
    trainer.setLastName(request.trainerLastName());
    trainer.setActive(request.trainerStatus());

    int trainingYear = request.trainingDate().getYear();
    int trainingMonth = request.trainingDate().getMonthValue();

    TrainerWorkloadYearSummary yearSummary = findOrCreateYearSummary(trainer, trainingYear);
    TrainerWorkloadMonthSummary monthSummary =
        findOrCreateMonthSummary(yearSummary, trainingMonth);

    monthSummary.setTrainingsSummaryDuration(calculateSummaryDuration(monthSummary, request));

    trainerWorkloadRepository.save(trainer);

    log.info(
        "Trainer workload updated, trainingId={}, trainingYear={}, trainingMonth={}, "
            + "summaryDuration={}",
        request.trainingId(),
        trainingYear,
        trainingMonth,
        monthSummary.getTrainingsSummaryDuration());
  }

  @Override
  public TrainerWorkloadResponse getTrainerWorkload(String username) {
    log.info("Getting trainer workload");
    TrainerWorkload trainer = trainerWorkloadRepository.findById(username)
        .orElseThrow(() -> new TrainerWorkloadNotFoundException(username));

    log.info("Trainer workload found, yearSummaryCount={}", trainer.getYears().size());
    return new TrainerWorkloadResponse(
        trainer.getUsername(),
        trainer.getFirstName(),
        trainer.getLastName(),
        trainer.isActive(),
        toYearResponses(trainer.getYears()));
  }

  private TrainerWorkloadYearSummary findOrCreateYearSummary(
      TrainerWorkload trainer,
      int trainingYear
  ) {
    return trainer.getYears().stream()
        .filter(summary -> summary.getYear() == trainingYear)
        .findFirst()
        .orElseGet(() -> {
          TrainerWorkloadYearSummary summary = TrainerWorkloadYearSummary.builder()
              .year(trainingYear)
              .build();
          trainer.getYears().add(summary);
          return summary;
        });
  }

  private TrainerWorkloadMonthSummary findOrCreateMonthSummary(
      TrainerWorkloadYearSummary yearSummary,
      int trainingMonth
  ) {
    return yearSummary.getMonths().stream()
        .filter(summary -> summary.getMonth() == trainingMonth)
        .findFirst()
        .orElseGet(() -> {
          TrainerWorkloadMonthSummary summary = TrainerWorkloadMonthSummary.builder()
              .month(trainingMonth)
              .trainingsSummaryDuration(0)
              .build();
          yearSummary.getMonths().add(summary);
          return summary;
        });
  }

  private int calculateSummaryDuration(
      TrainerWorkloadMonthSummary summary,
      TrainerWorkloadRequest request
  ) {
    int currentDuration = summary.getTrainingsSummaryDuration();

    if (request.actionType() == ActionType.ADD) {
      return currentDuration + request.trainingDuration();
    }

    int updatedDuration = currentDuration - request.trainingDuration();
    if (updatedDuration < 0) {
      throw new IllegalArgumentException("Training summary duration cannot be negative");
    }

    return updatedDuration;
  }

  private List<TrainerWorkloadYearResponse> toYearResponses(
      List<TrainerWorkloadYearSummary> summaries
  ) {
    return summaries.stream()
        .sorted(Comparator.comparingInt(TrainerWorkloadYearSummary::getYear))
        .map(summary -> new TrainerWorkloadYearResponse(
            summary.getYear(),
            toMonthResponses(summary.getMonths())))
        .toList();
  }

  private List<TrainerWorkloadMonthResponse> toMonthResponses(
      List<TrainerWorkloadMonthSummary> summaries
  ) {
    return summaries.stream()
        .sorted(Comparator.comparingInt(TrainerWorkloadMonthSummary::getMonth))
        .map(summary -> new TrainerWorkloadMonthResponse(
            summary.getMonth(),
            summary.getTrainingsSummaryDuration()))
        .toList();
  }
}
