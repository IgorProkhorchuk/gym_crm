package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.training.TrainingTypeResponse;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.logging.AuditContext;
import com.epam.gymcrm.web.api.TrainingTypeApi;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.TokenService;
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
public class TrainingTypeController implements TrainingTypeApi {

  private final GymFacade gymFacade;
  private final TokenService tokenService;

  @Autowired
  public TrainingTypeController(GymFacade gymFacade, TokenService tokenService) {
    this.gymFacade = gymFacade;
    this.tokenService = tokenService;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Override
  public List<TrainingTypeResponse> getTrainingTypes(
      @RequestHeader("X-Auth-Token") String token) {
    AuthenticatedUser user = tokenService.getUserByToken(token);
    AuditContext.setAuthenticatedUser(user.profileType(), user.userId(), user.profileId());
    return gymFacade.getTrainingTypes();
  }
}
