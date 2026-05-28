package com.epam.gymcrm.web.controller;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonNull;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.trainer.CreateTrainerRequest;
import com.epam.gymcrm.dto.trainer.TrainerProfileResponse;
import com.epam.gymcrm.dto.trainer.UpdateTrainerRequest;
import com.epam.gymcrm.dto.training.TrainerTrainingResponse;
import com.epam.gymcrm.dto.training.TrainerTrainingsRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.api.TrainerApi;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.FakeTokenService;
import com.epam.gymcrm.web.dto.ChangePasswordRestRequest;
import com.epam.gymcrm.web.dto.SwitchProfileStatusRestRequest;
import com.epam.gymcrm.web.dto.UpdateTrainerProfileRestRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trainers")
public class TrainerController implements TrainerApi {
  private final GymFacade gymFacade;
  private final FakeTokenService fakeTokenService;

  @Autowired
  public TrainerController(GymFacade gymFacade, FakeTokenService fakeTokenService) {
    this.gymFacade = gymFacade;
    this.fakeTokenService = fakeTokenService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Override
  public UsernamePasswordResponse createTrainer(@Valid @RequestBody CreateTrainerRequest request) {
    return gymFacade.createTrainer(request);
  }

  @GetMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public TrainerProfileResponse getTrainerProfile(
      @RequestHeader("X-Auth-Token") String token,
      @RequestParam(name = "username") String username) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException("Access denied");
    }
    requireNonBlank(username, "Username must not be blank");
    if (!user.username().equals(username)) {
      throw new AuthenticationException("Access denied");
    }

    AuthRequest request = new AuthRequest(username, user.password());
    return gymFacade.getTrainerProfile(request);
  }

  @PutMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public TrainerProfileResponse updateTrainerProfile(
      @RequestHeader("X-Auth-Token") String token,
      @Valid @RequestBody UpdateTrainerProfileRestRequest trainerRequest) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException("Access denied");
    }
    if (!user.username().equals(trainerRequest.username())) {
      throw new AuthenticationException("Access denied");
    }

    UpdateTrainerRequest request =
        new UpdateTrainerRequest(
            trainerRequest.username(),
            user.password(),
            trainerRequest.firstName(),
            trainerRequest.lastName(),
            trainerRequest.specialization(),
            trainerRequest.active());
    return gymFacade.updateTrainer(request);
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public void changePassword(
      @RequestHeader("X-Auth-Token") String token,
      @Valid @RequestBody ChangePasswordRestRequest body) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException("Access denied");
    }
    if (!user.username().equals(body.username())) {
      throw new AuthenticationException("Access denied");
    }

    ChangePasswordRequest request =
        new ChangePasswordRequest(body.username(), body.oldPassword(), body.newPassword());
    gymFacade.changeTrainerPassword(request);
    fakeTokenService.updatePassword(token, body.newPassword());
  }

  @PatchMapping("/profile/status")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public void switchActiveStatus(
      @RequestHeader("X-Auth-Token") String token,
      @Valid @RequestBody SwitchProfileStatusRestRequest request) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException("Access denied");
    }
    requireNonBlank(request.username(), "Username must not be blank");
    requireNonNull(request.active(), "Active status must not be null");
    if (!user.username().equals(request.username())) {
      throw new AuthenticationException("Access denied");
    }

    gymFacade.switchTrainerActiveStatus(new AuthRequest(user.username(), user.password()));
  }

  @GetMapping("/trainings")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public List<TrainerTrainingResponse> getTrainerTrainings(
      @RequestHeader("X-Auth-Token") String token,
      @RequestParam(name = "username") String username,
      @RequestParam(name = "fromDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(name = "toDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(name = "traineeName", required = false) String traineeName) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException("Access denied");
    }
    requireNonBlank(username, "Username must not be blank");
    if (!user.username().equals(username)) {
      throw new AuthenticationException("Access denied");
    }

    return gymFacade.getTrainerTrainings(
        new TrainerTrainingsRequest(
            username, user.password(), fromDate, toDate, traineeName, PageRequest.firstPage()));
  }
}
