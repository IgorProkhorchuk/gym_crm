package com.epam.gymcrm.client.workload;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadOutboxDispatcherTest {

  @InjectMocks private TrainerWorkloadOutboxDispatcher trainerWorkloadOutboxDispatcher;

  @Mock private TrainerWorkloadOutboxService trainerWorkloadOutboxService;

  @Test
  void dispatchPendingEventsShouldDelegateToOutboxService() {
    trainerWorkloadOutboxDispatcher.dispatchPendingEvents();

    verify(trainerWorkloadOutboxService).dispatchPendingEvents();
  }
}
