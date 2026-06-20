package com.epam.gymcrm.workload.controller;

import com.epam.gymcrm.workload.dto.TrainerWorkloadRequest;
import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trainer-workloads")
@RequiredArgsConstructor
public class TrainerWorkloadController {

  private final TrainerWorkloadService trainerWorkloadService;

  @PostMapping
  public ResponseEntity<Void> updateTrainerWorkload(
      @Valid @RequestBody TrainerWorkloadRequest request
  ) {
    trainerWorkloadService.updateTrainerWorkload(request);
    return ResponseEntity.ok().build();
  }
}
