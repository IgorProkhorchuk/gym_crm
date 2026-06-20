package com.epam.gymcrm.workload.service;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.model.TrainerMonthlySummary;
import com.epam.gymcrm.workload.model.TrainerWorkload;
import com.epam.gymcrm.workload.repository.TrainerMonthlySummaryRepository;
import com.epam.gymcrm.workload.repository.TrainerWorkloadRepository;
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
}
