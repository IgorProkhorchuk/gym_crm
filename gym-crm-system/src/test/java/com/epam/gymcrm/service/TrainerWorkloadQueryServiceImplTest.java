package com.epam.gymcrm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.client.workload.TrainerWorkloadClient;
import com.epam.gymcrm.dto.workload.TrainerWorkloadResponse;
import com.epam.gymcrm.service.impl.TrainerWorkloadQueryServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadQueryServiceImplTest {

  @InjectMocks private TrainerWorkloadQueryServiceImpl trainerWorkloadQueryService;

  @Mock private TrainerWorkloadClient trainerWorkloadClient;

  @Test
  void getTrainerWorkloadShouldReturnWorkloadSummaryFromClient() {
    TrainerWorkloadResponse response =
        new TrainerWorkloadResponse("Mike.Stone", "Mike", "Stone", true, List.of());
    when(trainerWorkloadClient.getTrainerWorkload("Mike.Stone")).thenReturn(response);

    TrainerWorkloadResponse result = trainerWorkloadQueryService.getTrainerWorkload("Mike.Stone");

    assertThat(result).isSameAs(response);
    verify(trainerWorkloadClient).getTrainerWorkload("Mike.Stone");
  }
}
