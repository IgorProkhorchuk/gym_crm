package com.epam.gymcrm.dto.workload;

import com.epam.gymcrm.util.SensitiveInfo;
import com.epam.gymcrm.util.SensitiveToString;
import java.util.List;

public record TrainerWorkloadResponse(
    @SensitiveInfo String trainerUsername,
    @SensitiveInfo String trainerFirstName,
    @SensitiveInfo String trainerLastName,
    boolean trainerStatus,
    List<TrainerWorkloadYearResponse> years
) {

  @Override
  public String toString() {
    return SensitiveToString.toString(this);
  }
}
