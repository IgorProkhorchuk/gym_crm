package com.epam.gymcrm.workload.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DtoPrivacyTest {

  @Test
  void workloadDtoToStringShouldRedactPersonalValues() {
    List<String> representations =
        List.of(
            new TrainerWorkloadRequest(
                    1L,
                    "Mike.Stone",
                    "Mike",
                    "Stone",
                    true,
                    LocalDate.of(2026, 5, 3),
                    60,
                    ActionType.ADD)
                .toString(),
            new TrainerWorkloadResponse("Mike.Stone", "Mike", "Stone", true, List.of())
                .toString());

    representations.forEach(
        representation ->
            assertThat(representation)
                .doesNotContain("Mike.Stone", "Mike", "Stone")
                .contains("[PROTECTED]"));
  }
}
