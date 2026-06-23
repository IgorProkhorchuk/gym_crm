package com.epam.gymcrm.workload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.workload.dto.ActionType;
import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.epam.gymcrm.workload.exception.TrainerWorkloadNotFoundException;
import com.epam.gymcrm.workload.model.TrainerMonthlySummary;
import com.epam.gymcrm.workload.model.TrainerWorkload;
import com.epam.gymcrm.workload.repository.TrainerMonthlySummaryRepository;
import com.epam.gymcrm.workload.repository.TrainerWorkloadRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceImplTest {

  private static final String USERNAME = "John.Doe";

  @InjectMocks private TrainerWorkloadServiceImpl trainerWorkloadService;

  @Mock private TrainerWorkloadRepository trainerWorkloadRepository;

  @Mock private TrainerMonthlySummaryRepository trainerMonthlySummaryRepository;

  @Test
  void updateTrainerWorkloadShouldCreateTrainerAndMonthlySummaryForAddAction() {
    TrainerWorkloadRequest request = request(ActionType.ADD, 60, true);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.empty());
    when(trainerMonthlySummaryRepository.findByTrainerUsernameAndTrainingYearAndTrainingMonth(
            USERNAME, 2026, 6))
        .thenReturn(Optional.empty());

    trainerWorkloadService.updateTrainerWorkload(request);

    ArgumentCaptor<TrainerWorkload> trainerCaptor =
        ArgumentCaptor.forClass(TrainerWorkload.class);
    ArgumentCaptor<TrainerMonthlySummary> summaryCaptor =
        ArgumentCaptor.forClass(TrainerMonthlySummary.class);
    verify(trainerWorkloadRepository).save(trainerCaptor.capture());
    verify(trainerMonthlySummaryRepository).save(summaryCaptor.capture());
    TrainerWorkload trainer = trainerCaptor.getValue();
    TrainerMonthlySummary summary = summaryCaptor.getValue();
    assertAll(
        () -> assertThat(trainer.getUsername()).isEqualTo(USERNAME),
        () -> assertThat(trainer.getFirstName()).isEqualTo("John"),
        () -> assertThat(trainer.getLastName()).isEqualTo("Doe"),
        () -> assertThat(trainer.isActive()).isTrue(),
        () -> assertThat(summary.getTrainer()).isSameAs(trainer),
        () -> assertThat(summary.getTrainingYear()).isEqualTo(2026),
        () -> assertThat(summary.getTrainingMonth()).isEqualTo(6),
        () -> assertThat(summary.getSummaryDuration()).isEqualTo(60));
  }

  @Test
  void updateTrainerWorkloadShouldIncreaseExistingMonthlySummaryForAddAction() {
    TrainerWorkload trainer = trainer();
    TrainerMonthlySummary summary = monthlySummary(trainer, 40);
    TrainerWorkloadRequest request = request(ActionType.ADD, 20, false);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
    when(trainerMonthlySummaryRepository.findByTrainerUsernameAndTrainingYearAndTrainingMonth(
            USERNAME, 2026, 6))
        .thenReturn(Optional.of(summary));

    trainerWorkloadService.updateTrainerWorkload(request);

    assertAll(
        () -> assertThat(summary.getSummaryDuration()).isEqualTo(60),
        () -> assertThat(trainer.getFirstName()).isEqualTo("John"),
        () -> assertThat(trainer.getLastName()).isEqualTo("Doe"),
        () -> assertThat(trainer.isActive()).isFalse());
    verify(trainerWorkloadRepository).save(trainer);
    verify(trainerMonthlySummaryRepository).save(summary);
  }

  @Test
  void updateTrainerWorkloadShouldDecreaseExistingMonthlySummaryForDeleteAction() {
    TrainerWorkload trainer = trainer();
    TrainerMonthlySummary summary = monthlySummary(trainer, 60);
    TrainerWorkloadRequest request = request(ActionType.DELETE, 15, true);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
    when(trainerMonthlySummaryRepository.findByTrainerUsernameAndTrainingYearAndTrainingMonth(
            USERNAME, 2026, 6))
        .thenReturn(Optional.of(summary));

    trainerWorkloadService.updateTrainerWorkload(request);

    assertThat(summary.getSummaryDuration()).isEqualTo(45);
    verify(trainerWorkloadRepository).save(trainer);
    verify(trainerMonthlySummaryRepository).save(summary);
  }

  @Test
  void updateTrainerWorkloadShouldRejectNegativeMonthlySummaryDuration() {
    TrainerWorkload trainer = trainer();
    TrainerMonthlySummary summary = monthlySummary(trainer, 10);
    TrainerWorkloadRequest request = request(ActionType.DELETE, 15, true);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
    when(trainerMonthlySummaryRepository.findByTrainerUsernameAndTrainingYearAndTrainingMonth(
            USERNAME, 2026, 6))
        .thenReturn(Optional.of(summary));

    assertThatThrownBy(() -> trainerWorkloadService.updateTrainerWorkload(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Training summary duration cannot be negative");

    verify(trainerWorkloadRepository, never()).save(any());
    verify(trainerMonthlySummaryRepository, never()).save(any());
  }

  @Test
  void getTrainerWorkloadShouldReturnSummaryGroupedByYearsAndMonths() {
    TrainerWorkload trainer = trainer();
    TrainerMonthlySummary maySummary = monthlySummary(trainer, 2025, 5, 30);
    TrainerMonthlySummary juneSummary = monthlySummary(trainer, 2026, 6, 60);
    TrainerMonthlySummary julySummary = monthlySummary(trainer, 2026, 7, 45);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));
    when(trainerMonthlySummaryRepository.findByTrainerUsernameOrderByTrainingYearAscTrainingMonthAsc(
            USERNAME))
        .thenReturn(List.of(maySummary, juneSummary, julySummary));

    TrainerWorkloadResponse response = trainerWorkloadService.getTrainerWorkload(USERNAME);

    assertAll(
        () -> assertThat(response.trainerUsername()).isEqualTo(USERNAME),
        () -> assertThat(response.trainerFirstName()).isEqualTo("Old"),
        () -> assertThat(response.trainerLastName()).isEqualTo("Name"),
        () -> assertThat(response.trainerStatus()).isTrue(),
        () -> assertThat(response.years()).hasSize(2),
        () -> assertThat(response.years().get(0).year()).isEqualTo(2025),
        () -> assertThat(response.years().get(0).months()).hasSize(1),
        () -> assertThat(response.years().get(0).months().getFirst().month()).isEqualTo(5),
        () ->
            assertThat(response.years().get(0).months().getFirst().trainingSummaryDuration())
                .isEqualTo(30),
        () -> assertThat(response.years().get(1).year()).isEqualTo(2026),
        () -> assertThat(response.years().get(1).months()).hasSize(2),
        () -> assertThat(response.years().get(1).months().getFirst().month()).isEqualTo(6),
        () ->
            assertThat(response.years().get(1).months().getFirst().trainingSummaryDuration())
                .isEqualTo(60),
        () -> assertThat(response.years().get(1).months().get(1).month()).isEqualTo(7),
        () ->
            assertThat(response.years().get(1).months().get(1).trainingSummaryDuration())
                .isEqualTo(45));
  }

  @Test
  void getTrainerWorkloadShouldThrowTrainerWorkloadNotFoundExceptionWhenTrainerDoesNotExist() {
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> trainerWorkloadService.getTrainerWorkload(USERNAME))
        .isInstanceOf(TrainerWorkloadNotFoundException.class)
        .hasMessage("Trainer workload not found: John.Doe");

    verify(trainerMonthlySummaryRepository, never())
        .findByTrainerUsernameOrderByTrainingYearAscTrainingMonthAsc(any());
  }

  private static TrainerWorkloadRequest request(
      ActionType actionType,
      int trainingDuration,
      boolean active
  ) {
    return new TrainerWorkloadRequest(
        USERNAME,
        "John",
        "Doe",
        active,
        LocalDate.of(2026, 6, 20),
        trainingDuration,
        actionType);
  }

  private static TrainerWorkload trainer() {
    return TrainerWorkload.builder()
        .username(USERNAME)
        .firstName("Old")
        .lastName("Name")
        .active(true)
        .build();
  }

  private static TrainerMonthlySummary monthlySummary(
      TrainerWorkload trainer,
      int summaryDuration
  ) {
    return monthlySummary(trainer, 2026, 6, summaryDuration);
  }

  private static TrainerMonthlySummary monthlySummary(
      TrainerWorkload trainer,
      int trainingYear,
      int trainingMonth,
      int summaryDuration
  ) {
    return TrainerMonthlySummary.builder()
        .trainer(trainer)
        .trainingYear(trainingYear)
        .trainingMonth(trainingMonth)
        .summaryDuration(summaryDuration)
        .build();
  }
}
