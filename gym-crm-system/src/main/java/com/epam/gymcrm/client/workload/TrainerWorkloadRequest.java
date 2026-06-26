package com.epam.gymcrm.client.workload;

import com.epam.gymcrm.util.SensitiveInfo;
import com.epam.gymcrm.util.SensitiveToString;
import java.time.LocalDate;

public record TrainerWorkloadRequest(
    Long trainingId,
    @SensitiveInfo String trainerUsername,
    @SensitiveInfo String trainerFirstName,
    @SensitiveInfo String trainerLastName,
    Boolean trainerStatus,
    LocalDate trainingDate,
    Integer trainingDuration,
    TrainerWorkloadActionType actionType
) {

  @Override
  public String toString() {
    return SensitiveToString.toString(this);
  }
}
