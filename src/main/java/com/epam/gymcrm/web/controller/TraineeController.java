package com.epam.gymcrm.web.controller;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;

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
import com.epam.gymcrm.web.api.TraineeApi;
import com.epam.gymcrm.web.auth.AuthenticatedPrincipal;
import com.epam.gymcrm.web.auth.AuthenticatedUserProvider;
import com.epam.gymcrm.web.dto.ChangePasswordRestRequest;
import com.epam.gymcrm.web.dto.DeleteProfileRestRequest;
import com.epam.gymcrm.web.dto.SwitchProfileStatusRestRequest;
import com.epam.gymcrm.web.dto.UpdateTraineeProfileRestRequest;
import com.epam.gymcrm.web.dto.UpdateTraineeTrainersRestRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trainees")
@RequiredArgsConstructor
public class TraineeController implements TraineeApi {

  private static final String TRAINEE_PROFILE_REQUIRED =
      "This operation is available only for trainee profiles";
  private static final String OWN_TRAINEE_PROFILE_REQUIRED =
      "Authenticated trainee can perform this operation only for own profile";

  private final GymFacade gymFacade;
  private final AuthenticatedUserProvider authenticatedUserProvider;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Override
  public UsernamePasswordResponse createTrainee(@Valid @RequestBody CreateTraineeRequest request) {
    return gymFacade.createTrainee(request);
  }

  @GetMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public TraineeProfileResponse getTraineeProfile(@RequestParam(name = "username") String username) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException(TRAINEE_PROFILE_REQUIRED);
    }
    requireNonBlank(username, "Username must not be blank");
    if (!user.username().equals(username)) {
      throw new AuthenticationException(OWN_TRAINEE_PROFILE_REQUIRED);
    }

    AuthRequest request = new AuthRequest(username);
    return gymFacade.getTraineeProfile(request);
  }

  @PutMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public TraineeProfileResponse updateTraineeProfile(
      @Valid @RequestBody UpdateTraineeProfileRestRequest traineeRequest) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException(TRAINEE_PROFILE_REQUIRED);
    }
    if (!user.username().equals(traineeRequest.username())) {
      throw new AuthenticationException(OWN_TRAINEE_PROFILE_REQUIRED);
    }

    UpdateTraineeRequest request =
        new UpdateTraineeRequest(
            traineeRequest.username(),
            traineeRequest.firstName(),
            traineeRequest.lastName(),
            traineeRequest.dateOfBirth(),
            traineeRequest.address(),
            traineeRequest.active());
    return gymFacade.updateTrainee(request);
  }

  @DeleteMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public void deleteProfile(@Valid @RequestBody DeleteProfileRestRequest request) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException(TRAINEE_PROFILE_REQUIRED);
    }
    if (!user.username().equals(request.username())) {
      throw new AuthenticationException(OWN_TRAINEE_PROFILE_REQUIRED);
    }
    AuthRequest authRequest = new AuthRequest(user.username());
    gymFacade.deleteTraineeByUsername(authRequest);
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public void changePassword(@Valid @RequestBody ChangePasswordRestRequest body) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException(TRAINEE_PROFILE_REQUIRED);
    }
    if (!user.username().equals(body.username())) {
      throw new AuthenticationException(OWN_TRAINEE_PROFILE_REQUIRED);
    }

    ChangePasswordRequest request =
        new ChangePasswordRequest(body.username(), body.oldPassword(), body.newPassword());
    gymFacade.changeTraineePassword(request);
  }

  @PatchMapping("/profile/status")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public void switchActiveStatus(@Valid @RequestBody SwitchProfileStatusRestRequest request) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException(TRAINEE_PROFILE_REQUIRED);
    }
    if (!user.username().equals(request.username())) {
      throw new AuthenticationException(OWN_TRAINEE_PROFILE_REQUIRED);
    }

    gymFacade.switchTraineeActiveStatus(new AuthRequest(user.username()));
  }

  @GetMapping("/trainers/unassigned")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public List<TrainerSummaryResponse> getUnassignedTrainers(
      @RequestParam(name = "username") String username) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException(TRAINEE_PROFILE_REQUIRED);
    }
    requireNonBlank(username, "Username must not be blank");
    if (!user.username().equals(username)) {
      throw new AuthenticationException(OWN_TRAINEE_PROFILE_REQUIRED);
    }
    return gymFacade.getUnassignedTrainers(new AuthRequest(username));
  }

  @PutMapping("/trainers")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public List<TrainerSummaryResponse> updateTraineeTrainers(
      @Valid @RequestBody UpdateTraineeTrainersRestRequest request) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException(TRAINEE_PROFILE_REQUIRED);
    }
    if (!user.username().equals(request.traineeUsername())) {
      throw new AuthenticationException(OWN_TRAINEE_PROFILE_REQUIRED);
    }

    return gymFacade.updateTraineeTrainers(
        new UpdateTraineeTrainersRequest(request.traineeUsername(), request.trainerUsernames()));
  }

  @GetMapping("/trainings")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public List<TraineeTrainingResponse> getTraineeTrainings(
      @RequestParam(name = "username") String username,
      @RequestParam(name = "fromDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(name = "toDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(name = "trainerName", required = false) String trainerName,
      @RequestParam(name = "trainingType", required = false) String trainingType) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException(TRAINEE_PROFILE_REQUIRED);
    }
    requireNonBlank(username, "Username must not be blank");
    if (!user.username().equals(username)) {
      throw new AuthenticationException(OWN_TRAINEE_PROFILE_REQUIRED);
    }

    return gymFacade.getTraineeTrainings(
        new TraineeTrainingsRequest(
            username,
            fromDate,
            toDate,
            trainerName,
            trainingType,
            PageRequest.firstPage()));
  }
}
