package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.workload.TrainerWorkloadResponse;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.api.TrainerWorkloadApi;
import com.epam.gymcrm.web.auth.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trainer-workloads")
@RequiredArgsConstructor
public class TrainerWorkloadController implements TrainerWorkloadApi {

  private final GymFacade gymFacade;
  private final AuthenticatedUserProvider authenticatedUserProvider;

  @GetMapping("/{username}")
  @Override
  public TrainerWorkloadResponse getTrainerWorkload(@PathVariable String username) {
    authenticatedUserProvider.currentUser();
    return gymFacade.getTrainerWorkload(username);
  }
}
