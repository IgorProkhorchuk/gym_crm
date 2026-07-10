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
import com.epam.gymcrm.workload.model.TrainerWorkload;
import com.epam.gymcrm.workload.model.TrainerWorkloadMonthSummary;
import com.epam.gymcrm.workload.model.TrainerWorkloadProcessedEvent;
import com.epam.gymcrm.workload.model.TrainerWorkloadYearSummary;
import com.epam.gymcrm.workload.repository.TrainerWorkloadProcessedEventRepository;
import com.epam.gymcrm.workload.repository.TrainerWorkloadRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceImplTest {

  private static final String USERNAME = "John.Doe";

  private TrainerWorkloadServiceImpl trainerWorkloadService;
  private SimpleMeterRegistry meterRegistry;

  @Mock private TrainerWorkloadRepository trainerWorkloadRepository;

  @Mock private TrainerWorkloadProcessedEventRepository processedEventRepository;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    trainerWorkloadService = new TrainerWorkloadServiceImpl(
        trainerWorkloadRepository,
        processedEventRepository,
        new TransactionTemplate(noOpTransactionManager()),
        meterRegistry);
  }

  @Test
  void updateTrainerWorkloadShouldCreateTrainerAndMonthlySummaryForAddAction() {
    TrainerWorkloadRequest request = request(ActionType.ADD, 60, true);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.empty());

    trainerWorkloadService.updateTrainerWorkload(request);

    ArgumentCaptor<TrainerWorkload> trainerCaptor =
        ArgumentCaptor.forClass(TrainerWorkload.class);
    verify(trainerWorkloadRepository).save(trainerCaptor.capture());
    verify(processedEventRepository).insert(any(TrainerWorkloadProcessedEvent.class));

    TrainerWorkload trainer = trainerCaptor.getValue();
    TrainerWorkloadYearSummary year = trainer.getYears().getFirst();
    TrainerWorkloadMonthSummary month = year.getMonths().getFirst();
    assertAll(
        () -> assertThat(trainer.getUsername()).isEqualTo(USERNAME),
        () -> assertThat(trainer.getFirstName()).isEqualTo("John"),
        () -> assertThat(trainer.getLastName()).isEqualTo("Doe"),
        () -> assertThat(trainer.isActive()).isTrue(),
        () -> assertThat(year.getYear()).isEqualTo(2026),
        () -> assertThat(month.getMonth()).isEqualTo(6),
        () -> assertThat(month.getTrainingsSummaryDuration()).isEqualTo(60));
  }

  @Test
  void updateTrainerWorkloadShouldIncreaseExistingMonthlySummaryForAddAction() {
    TrainerWorkload trainer = trainerWithSummary(2026, 6, 40);
    TrainerWorkloadRequest request = request(ActionType.ADD, 20, false);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));

    trainerWorkloadService.updateTrainerWorkload(request);

    TrainerWorkloadMonthSummary month = trainer.getYears().getFirst().getMonths().getFirst();
    assertAll(
        () -> assertThat(month.getTrainingsSummaryDuration()).isEqualTo(60),
        () -> assertThat(trainer.getFirstName()).isEqualTo("John"),
        () -> assertThat(trainer.getLastName()).isEqualTo("Doe"),
        () -> assertThat(trainer.isActive()).isFalse());
    verify(trainerWorkloadRepository).save(trainer);
    verify(processedEventRepository).insert(any(TrainerWorkloadProcessedEvent.class));
  }

  @Test
  void updateTrainerWorkloadShouldCreateMissingYearAndMonthForExistingTrainer() {
    TrainerWorkload trainer = trainerWithSummary(2025, 5, 30);
    TrainerWorkloadRequest request = request(ActionType.ADD, 20, true);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));

    trainerWorkloadService.updateTrainerWorkload(request);

    assertAll(
        () -> assertThat(trainer.getYears()).hasSize(2),
        () -> assertThat(trainer.getYears().get(1).getYear()).isEqualTo(2026),
        () -> assertThat(trainer.getYears().get(1).getMonths()).hasSize(1),
        () -> assertThat(trainer.getYears().get(1).getMonths().getFirst().getMonth())
            .isEqualTo(6),
        () -> assertThat(trainer.getYears().get(1).getMonths().getFirst()
            .getTrainingsSummaryDuration()).isEqualTo(20));
    verify(trainerWorkloadRepository).save(trainer);
    verify(processedEventRepository).insert(any(TrainerWorkloadProcessedEvent.class));
  }

  @Test
  void updateTrainerWorkloadShouldCreateMissingMonthForExistingYear() {
    TrainerWorkload trainer = trainerWithSummary(2026, 5, 30);
    TrainerWorkloadRequest request = request(ActionType.ADD, 20, true);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));

    trainerWorkloadService.updateTrainerWorkload(request);

    TrainerWorkloadYearSummary year = trainer.getYears().getFirst();
    assertAll(
        () -> assertThat(trainer.getYears()).hasSize(1),
        () -> assertThat(year.getMonths()).hasSize(2),
        () -> assertThat(year.getMonths().get(1).getMonth()).isEqualTo(6),
        () -> assertThat(year.getMonths().get(1).getTrainingsSummaryDuration()).isEqualTo(20));
    verify(trainerWorkloadRepository).save(trainer);
    verify(processedEventRepository).insert(any(TrainerWorkloadProcessedEvent.class));
  }

  @Test
  void updateTrainerWorkloadShouldDecreaseExistingMonthlySummaryForDeleteAction() {
    TrainerWorkload trainer = trainerWithSummary(2026, 6, 60);
    TrainerWorkloadRequest request = request(ActionType.DELETE, 15, true);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));

    trainerWorkloadService.updateTrainerWorkload(request);

    TrainerWorkloadMonthSummary month = trainer.getYears().getFirst().getMonths().getFirst();
    assertThat(month.getTrainingsSummaryDuration()).isEqualTo(45);
    verify(trainerWorkloadRepository).save(trainer);
    verify(processedEventRepository).insert(any(TrainerWorkloadProcessedEvent.class));
  }

  @Test
  void updateTrainerWorkloadShouldIgnoreAlreadyProcessedEvent() {
    TrainerWorkloadRequest request = request(ActionType.ADD, 20, true);
    when(processedEventRepository.insert(any(TrainerWorkloadProcessedEvent.class)))
        .thenThrow(new DuplicateKeyException("duplicate event"));

    trainerWorkloadService.updateTrainerWorkload(request);

    verify(trainerWorkloadRepository, never()).findById(any());
    verify(trainerWorkloadRepository, never()).save(any());
    verify(processedEventRepository).insert(any(TrainerWorkloadProcessedEvent.class));
    assertThat(meterRegistry.get("trainer.workload.duplicate.events")
        .tag("actionType", "ADD")
        .counter()
        .count()).isEqualTo(1);
  }

  @Test
  void updateTrainerWorkloadShouldRejectNegativeMonthlySummaryDuration() {
    TrainerWorkload trainer = trainerWithSummary(2026, 6, 10);
    TrainerWorkloadRequest request = request(ActionType.DELETE, 15, true);
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));

    assertThatThrownBy(() -> trainerWorkloadService.updateTrainerWorkload(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Training summary duration cannot be negative");

    verify(trainerWorkloadRepository, never()).save(any());
    verify(processedEventRepository).insert(any(TrainerWorkloadProcessedEvent.class));
  }

  @Test
  void getTrainerWorkloadShouldReturnSummaryGroupedByYearsAndMonths() {
    TrainerWorkload trainer = trainer();
    trainer.setYears(new ArrayList<>(List.of(
        yearSummary(2026, monthSummary(7, 45), monthSummary(6, 60)),
        yearSummary(2025, monthSummary(5, 30)))));
    when(trainerWorkloadRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));

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
  }

  private static TrainerWorkloadRequest request(
      ActionType actionType,
      int trainingDuration,
      boolean active
  ) {
    return new TrainerWorkloadRequest(
        1L,
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

  private static TrainerWorkload trainerWithSummary(
      int trainingYear,
      int trainingMonth,
      int summaryDuration
  ) {
    TrainerWorkload trainer = trainer();
    trainer.setYears(new ArrayList<>(List.of(yearSummary(
        trainingYear,
        monthSummary(trainingMonth, summaryDuration)))));
    return trainer;
  }

  private static TrainerWorkloadYearSummary yearSummary(
      int trainingYear,
      TrainerWorkloadMonthSummary... months
  ) {
    return TrainerWorkloadYearSummary.builder()
        .year(trainingYear)
        .months(new ArrayList<>(List.of(months)))
        .build();
  }

  private static TrainerWorkloadMonthSummary monthSummary(
      int trainingMonth,
      int summaryDuration
  ) {
    return TrainerWorkloadMonthSummary.builder()
        .month(trainingMonth)
        .trainingsSummaryDuration(summaryDuration)
        .build();
  }

  private static PlatformTransactionManager noOpTransactionManager() {
    return new PlatformTransactionManager() {
      @Override
      public TransactionStatus getTransaction(TransactionDefinition definition) {
        return new SimpleTransactionStatus();
      }

      @Override
      public void commit(TransactionStatus status) {}

      @Override
      public void rollback(TransactionStatus status) {}
    };
  }
}
