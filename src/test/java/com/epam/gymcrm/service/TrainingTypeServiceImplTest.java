package com.epam.gymcrm.service;

import static com.epam.gymcrm.TestFixtures.trainingType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.training.TrainingTypeResponse;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.service.impl.TrainingTypeServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

  @InjectMocks private TrainingTypeServiceImpl trainingTypeService;

  @Mock private TrainingTypeDao trainingTypeDao;

  @Test
  void getTrainingTypesShouldReturnMappedDaoResult() {
    TrainingType fitness = trainingType("Fitness");
    fitness.setTrainingTypeId(1L);
    TrainingType yoga = trainingType("Yoga");
    yoga.setTrainingTypeId(2L);
    when(trainingTypeDao.findAll()).thenReturn(List.of(fitness, yoga));

    List<TrainingTypeResponse> result = trainingTypeService.getTrainingTypes();

    assertAll(
        () ->
            assertThat(result)
                .containsExactly(
                    new TrainingTypeResponse(1L, "Fitness"),
                    new TrainingTypeResponse(2L, "Yoga")),
        () -> verify(trainingTypeDao).findAll());
  }
}
