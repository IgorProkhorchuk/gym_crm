package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.FakeTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trainees")
public class TraineeController {

  private final GymFacade gymFacade;
  private final FakeTokenService fakeTokenService;

  @Autowired
  public TraineeController(GymFacade gymFacade, FakeTokenService fakeTokenService) {
    this.gymFacade = gymFacade;
    this.fakeTokenService = fakeTokenService;
  }


  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UsernamePasswordResponse createTrainee(@RequestBody CreateTraineeRequest request) {
    return gymFacade.createTrainee(request);
  }

  @GetMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  public TraineeProfileResponse getTraineeProfile(@RequestHeader("X-Auth-Token") String token) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException("Access denied");
    }

    AuthRequest request = new AuthRequest(user.username(), user.password());
    return gymFacade.getTraineeProfile(request);
  }

}
