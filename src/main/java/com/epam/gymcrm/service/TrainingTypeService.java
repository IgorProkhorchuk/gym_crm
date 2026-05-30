package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.training.TrainingTypeResponse;
import java.util.List;

/** Business operations for training type reference data. */
public interface TrainingTypeService {

  /**
   * Returns all available training types.
   *
   * @return training type reference data
   */
  List<TrainingTypeResponse> getTrainingTypes();
}
