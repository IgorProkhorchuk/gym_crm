package com.epam.gymcrm.workload.service;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadMonthResponse;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.epam.gymcrm.workload.dto.TrainerWorkloadYearResponse;
import com.epam.gymcrm.workload.exception.TrainerWorkloadNotFoundException;
import com.epam.gymcrm.workload.model.TrainerMonthlySummary;
import com.epam.gymcrm.workload.model.TrainerWorkload;
import com.epam.gymcrm.workload.repository.TrainerMonthlySummaryRepository;
import com.epam.gymcrm.workload.repository.TrainerWorkloadRepository;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

  private final TrainerWorkloadRepository trainerWorkloadRepository;
  private final TrainerMonthlySummaryRepository trainerMonthlySummaryRepository;

  @Override
  @Transactional
  public void updateTrainerWorkload(TrainerWorkloadRequest request) {
    TrainerWorkload trainer = trainerWorkloadRepository.findById(request.trainerUsername())
        .orElseGet(() -> TrainerWorkload.builder()
            .username(request.trainerUsername())
            .build());

    trainer.setFirstName(request.trainerFirstName());
    trainer.setLastName(request.trainerLastName());
    trainer.setActive(request.trainerStatus());

    int trainingYear = request.trainingDate().getYear();
    int trainingMonth = request.trainingDate().getMonthValue();

    TrainerMonthlySummary summary = trainerMonthlySummaryRepository
        .findByTrainerUsernameAndTrainingYearAndTrainingMonth(
            request.trainerUsername(),
            trainingYear,
            trainingMonth
        )
        .orElseGet(() -> TrainerMonthlySummary.builder()
            .trainer(trainer)
            .trainingYear(trainingYear)
            .trainingMonth(trainingMonth)
            .summaryDuration(0)
            .build());

    summary.setSummaryDuration(calculateSummaryDuration(summary, request));

    trainerWorkloadRepository.save(trainer);
    trainerMonthlySummaryRepository.save(summary);
  }

  @Override
  @Transactional(readOnly = true)
  public TrainerWorkloadResponse getTrainerWorkload(String username) {
    TrainerWorkload trainer = trainerWorkloadRepository.findById(username)
        .orElseThrow(() -> new TrainerWorkloadNotFoundException(username));
    List<TrainerMonthlySummary> summaries = trainerMonthlySummaryRepository
        .findByTrainerUsernameOrderByTrainingYearAscTrainingMonthAsc(username);

    return new TrainerWorkloadResponse(
        trainer.getUsername(),
        trainer.getFirstName(),
        trainer.getLastName(),
        trainer.isActive(),
        toYearResponses(summaries));
  }

  private int calculateSummaryDuration(
      TrainerMonthlySummary summary,
      TrainerWorkloadRequest request
  ) {
    int currentDuration = summary.getSummaryDuration();

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
      List<TrainerMonthlySummary> summaries
  ) {
    Map<Integer, List<TrainerMonthlySummary>> summariesByYear = summaries.stream()
        .collect(Collectors.groupingBy(
            TrainerMonthlySummary::getTrainingYear,
            TreeMap::new,
            Collectors.toList()));

    return summariesByYear.entrySet().stream()
        .map(entry -> new TrainerWorkloadYearResponse(
            entry.getKey(),
            toMonthResponses(entry.getValue())))
        .toList();
  }

  private List<TrainerWorkloadMonthResponse> toMonthResponses(
      List<TrainerMonthlySummary> summaries
  ) {
    return summaries.stream()
        .map(summary -> new TrainerWorkloadMonthResponse(
            summary.getTrainingMonth(),
            summary.getSummaryDuration()))
        .toList();
  }
}
