package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.training.TrainingTypeResponse;
import com.epam.gymcrm.service.TrainingTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingTypeServiceImpl implements TrainingTypeService {

  private final TrainingTypeDao trainingTypeDao;

  @Override
  @Transactional(readOnly = true)
  public List<TrainingTypeResponse> getTrainingTypes() {
    log.info("Getting training types");
    return trainingTypeDao.findAll().stream()
        .map(type -> new TrainingTypeResponse(type.getTrainingTypeId(), type.getTrainingTypeName()))
        .toList();
  }
}
