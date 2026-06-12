package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.training.TrainingTypeResponse;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.api.TrainingTypeApi;
import com.epam.gymcrm.web.auth.AuthenticatedUserProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/training-types")
@RequiredArgsConstructor
public class TrainingTypeController implements TrainingTypeApi {

  private final GymFacade gymFacade;
  private final AuthenticatedUserProvider authenticatedUserProvider;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Override
  public List<TrainingTypeResponse> getTrainingTypes() {
    authenticatedUserProvider.currentUser();
    return gymFacade.getTrainingTypes();
  }
}
