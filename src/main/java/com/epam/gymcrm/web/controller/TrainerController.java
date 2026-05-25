package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.FakeTokenService;
import com.epam.gymcrm.web.dto.ChangePasswordRestRequest;
import com.epam.gymcrm.web.dto.UpdateTrainerProfileRestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trainers")
public class TrainerController {
  private final GymFacade gymFacade;
  private final FakeTokenService fakeTokenService;

  @Autowired
  public TrainerController(GymFacade gymFacade, FakeTokenService fakeTokenService) {
    this.gymFacade = gymFacade;
    this.fakeTokenService = fakeTokenService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UsernamePasswordResponse createTrainer(@RequestBody CreateTrainerRequest request) {
    return gymFacade.createTrainer(request);
  }

  @GetMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  public TrainerProfileResponse getTrainerProfile(@RequestHeader("X-Auth-Token") String token) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException("Access denied");
    }

    AuthRequest request = new AuthRequest(user.username(), user.password());
    return gymFacade.getTrainerProfile(request);
  }

  @PutMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  public TrainerProfileResponse updateTrainerProfile(@RequestHeader("X-Auth-Token") String token,
                                                     @RequestBody UpdateTrainerProfileRestRequest trainerRequest) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException("Access denied");
    }
    if (!user.username().equals(trainerRequest.username())) {
      throw new AuthenticationException("Access denied");
    }

    UpdateTrainerRequest request = new UpdateTrainerRequest(
        trainerRequest.username(),
        user.password(),
        trainerRequest.firstName(),
        trainerRequest.lastName(),
        trainerRequest.specialization(),
        trainerRequest.active()
    );
    return gymFacade.updateTrainer(request);
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.OK)
  public void changePassword(@RequestHeader("X-Auth-Token") String token,
                             @RequestBody ChangePasswordRestRequest body) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException("Access denied");
    }
    if (!user.username().equals(body.username())) {
      throw new AuthenticationException("Access denied");
    }

    ChangePasswordRequest request = new ChangePasswordRequest(body.username(), body.oldPassword(), body.newPassword());
    gymFacade.changeTrainerPassword(request);
    fakeTokenService.updatePassword(token, body.newPassword());
  }
}
