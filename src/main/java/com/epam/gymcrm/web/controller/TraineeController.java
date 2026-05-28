package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.AuthRequest;
import com.epam.gymcrm.dto.ChangePasswordRequest;
import com.epam.gymcrm.dto.PageRequest;
import com.epam.gymcrm.dto.UsernamePasswordResponse;
import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.trainee.CreateTraineeRequest;
import com.epam.gymcrm.dto.trainee.TraineeProfileResponse;
import com.epam.gymcrm.dto.trainee.UpdateTraineeRequest;
import com.epam.gymcrm.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.gymcrm.dto.trainer.TrainerSummaryResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingResponse;
import com.epam.gymcrm.dto.training.TraineeTrainingsRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.FakeTokenService;
import com.epam.gymcrm.web.dto.ChangePasswordRestRequest;
import com.epam.gymcrm.web.dto.DeleteProfileRestRequest;
import com.epam.gymcrm.web.dto.SwitchProfileStatusRestRequest;
import com.epam.gymcrm.web.dto.UpdateTraineeProfileRestRequest;
import com.epam.gymcrm.web.dto.UpdateTraineeTrainersRestRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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

  @PutMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  public TraineeProfileResponse updateTraineeProfile(
      @RequestHeader("X-Auth-Token") String token,
      @RequestBody UpdateTraineeProfileRestRequest traineeRequest) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException("Access denied");
    }
    if (!user.username().equals(traineeRequest.username())) {
      throw new AuthenticationException("Access denied");
    }

    UpdateTraineeRequest request =
        new UpdateTraineeRequest(
            traineeRequest.username(),
            user.password(),
            traineeRequest.firstName(),
            traineeRequest.lastName(),
            traineeRequest.dateOfBirth(),
            traineeRequest.address(),
            traineeRequest.active());
    return gymFacade.updateTrainee(request);
  }

  @DeleteMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  public void deleteProfile(
      @RequestHeader("X-Auth-Token") String token, @RequestBody DeleteProfileRestRequest request) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException("Access denied");
    }
    if (!user.username().equals(request.username())) {
      throw new AuthenticationException("Access denied");
    }
    AuthRequest authRequest = new AuthRequest(user.username(), user.password());
    gymFacade.deleteTraineeByUsername(authRequest);
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.OK)
  public void changePassword(
      @RequestHeader("X-Auth-Token") String token, @RequestBody ChangePasswordRestRequest body) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException("Access denied");
    }
    if (!user.username().equals(body.username())) {
      throw new AuthenticationException("Access denied");
    }

    ChangePasswordRequest request =
        new ChangePasswordRequest(body.username(), body.oldPassword(), body.newPassword());
    gymFacade.changeTraineePassword(request);
    fakeTokenService.updatePassword(token, body.newPassword());
  }

  @PatchMapping("/profile/status")
  @ResponseStatus(HttpStatus.OK)
  public void switchActiveStatus(
      @RequestHeader("X-Auth-Token") String token,
      @RequestBody SwitchProfileStatusRestRequest request) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException("Access denied");
    }
    if (request.username() == null || request.username().isBlank()) {
      throw new IllegalArgumentException("Username must not be blank");
    }
    if (request.active() == null) {
      throw new IllegalArgumentException("Active status must not be null");
    }
    if (!user.username().equals(request.username())) {
      throw new AuthenticationException("Access denied");
    }

    gymFacade.switchTraineeActiveStatus(new AuthRequest(user.username(), user.password()));
  }

  @GetMapping("/trainers/unassigned")
  @ResponseStatus(HttpStatus.OK)
  public List<TrainerSummaryResponse> getUnassignedTrainers(
      @RequestHeader("X-Auth-Token") String token) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException("Access denied");
    }
    return gymFacade.getUnassignedTrainers(new AuthRequest(user.username(), user.password()));
  }

  @PutMapping("/trainers")
  @ResponseStatus(HttpStatus.OK)
  public List<TrainerSummaryResponse> updateTraineeTrainers(
      @RequestHeader("X-Auth-Token") String token,
      @RequestBody UpdateTraineeTrainersRestRequest request) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException("Access denied");
    }
    if (request.traineeUsername() == null || request.traineeUsername().isBlank()) {
      throw new IllegalArgumentException("Trainee username must not be blank");
    }
    if (request.trainerUsernames() == null) {
      throw new IllegalArgumentException("Trainer usernames must not be null");
    }
    if (request.trainerUsernames().stream()
        .anyMatch(username -> username == null || username.isBlank())) {
      throw new IllegalArgumentException("Trainer username must not be blank");
    }
    if (!user.username().equals(request.traineeUsername())) {
      throw new AuthenticationException("Access denied");
    }

    return gymFacade.updateTraineeTrainers(
        new UpdateTraineeTrainersRequest(
            request.traineeUsername(), user.password(), request.trainerUsernames()));
  }

  @GetMapping("/trainings")
  @ResponseStatus(HttpStatus.OK)
  public List<TraineeTrainingResponse> getTraineeTrainings(
      @RequestHeader("X-Auth-Token") String token,
      @RequestParam(name = "username") String username,
      @RequestParam(name = "fromDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(name = "toDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(name = "trainerName", required = false) String trainerName,
      @RequestParam(name = "trainingType", required = false) String trainingType) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException("Access denied");
    }
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Username must not be blank");
    }
    if (!user.username().equals(username)) {
      throw new AuthenticationException("Access denied");
    }

    return gymFacade.getTraineeTrainings(
        new TraineeTrainingsRequest(
            username,
            user.password(),
            fromDate,
            toDate,
            trainerName,
            trainingType,
            PageRequest.firstPage()));
  }
}
