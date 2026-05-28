package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.training.TrainingTypeResponse;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.FakeTokenService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/training-types")
public class TrainingTypeController {

  private final GymFacade gymFacade;
  private final FakeTokenService fakeTokenService;

  @Autowired
  public TrainingTypeController(GymFacade gymFacade, FakeTokenService fakeTokenService) {
    this.gymFacade = gymFacade;
    this.fakeTokenService = fakeTokenService;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<TrainingTypeResponse> getTrainingTypes(
      @RequestHeader("X-Auth-Token") String token) {
    fakeTokenService.getUserByToken(token);
    return gymFacade.getTrainingTypes();
  }
}
