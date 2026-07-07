package com.epam.gymcrm.workload.controller;

import com.epam.gymcrm.workload.api.TrainerWorkloadApi;
import com.epam.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trainer-workloads")
@RequiredArgsConstructor
public class TrainerWorkloadController implements TrainerWorkloadApi {

  private final TrainerWorkloadService trainerWorkloadService;

  @GetMapping("/{username}")
  @Override
  public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(
      @PathVariable(name = "username") String username
  ) {
    return ResponseEntity.ok(trainerWorkloadService.getTrainerWorkload(username));
  }
}
