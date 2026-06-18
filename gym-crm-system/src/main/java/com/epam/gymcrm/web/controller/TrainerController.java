package com.epam.gymcrm.web.controller;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;

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
import com.epam.gymcrm.web.auth.AuthenticatedPrincipal;
import com.epam.gymcrm.web.auth.AuthenticatedUserProvider;
import com.epam.gymcrm.web.dto.ChangePasswordRestRequest;
import com.epam.gymcrm.web.dto.SwitchProfileStatusRestRequest;
import com.epam.gymcrm.web.dto.UpdateTrainerProfileRestRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/v1/trainers")
@RequiredArgsConstructor
public class TrainerController implements TrainerApi {

  private static final String TRAINER_PROFILE_REQUIRED =
      "This operation is available only for trainer profiles";
  private static final String OWN_TRAINER_PROFILE_REQUIRED =
      "Authenticated trainer can perform this operation only for own profile";

  private final GymFacade gymFacade;
  private final AuthenticatedUserProvider authenticatedUserProvider;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Override
  public UsernamePasswordResponse createTrainer(@Valid @RequestBody CreateTrainerRequest request) {
    return gymFacade.createTrainer(request);
  }

  @GetMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public TrainerProfileResponse getTrainerProfile(@RequestParam(name = "username") String username) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException(TRAINER_PROFILE_REQUIRED);
    }
    requireNonBlank(username, "Username must not be blank");
    if (!user.username().equals(username)) {
      throw new AuthenticationException(OWN_TRAINER_PROFILE_REQUIRED);
    }

    AuthRequest request = new AuthRequest(username);
    return gymFacade.getTrainerProfile(request);
  }

  @PutMapping("/profile")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public TrainerProfileResponse updateTrainerProfile(
      @Valid @RequestBody UpdateTrainerProfileRestRequest trainerRequest) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException(TRAINER_PROFILE_REQUIRED);
    }
    if (!user.username().equals(trainerRequest.username())) {
      throw new AuthenticationException(OWN_TRAINER_PROFILE_REQUIRED);
    }

    UpdateTrainerRequest request =
        new UpdateTrainerRequest(
            trainerRequest.username(),
            trainerRequest.firstName(),
            trainerRequest.lastName(),
            trainerRequest.specialization(),
            trainerRequest.active());
    return gymFacade.updateTrainer(request);
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public void changePassword(@Valid @RequestBody ChangePasswordRestRequest body) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException(TRAINER_PROFILE_REQUIRED);
    }
    if (!user.username().equals(body.username())) {
      throw new AuthenticationException(OWN_TRAINER_PROFILE_REQUIRED);
    }

    ChangePasswordRequest request =
        new ChangePasswordRequest(body.username(), body.oldPassword(), body.newPassword());
    gymFacade.changeTrainerPassword(request);
  }

  @PatchMapping("/profile/status")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public void switchActiveStatus(@Valid @RequestBody SwitchProfileStatusRestRequest request) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException(TRAINER_PROFILE_REQUIRED);
    }
    if (!user.username().equals(request.username())) {
      throw new AuthenticationException(OWN_TRAINER_PROFILE_REQUIRED);
    }

    gymFacade.switchTrainerActiveStatus(new AuthRequest(user.username()));
  }

  @GetMapping("/trainings")
  @ResponseStatus(HttpStatus.OK)
  @Override
  public List<TrainerTrainingResponse> getTrainerTrainings(
      @RequestParam(name = "username") String username,
      @RequestParam(name = "fromDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(name = "toDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(name = "traineeName", required = false) String traineeName) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINER) {
      throw new AuthenticationException(TRAINER_PROFILE_REQUIRED);
    }
    requireNonBlank(username, "Username must not be blank");
    if (!user.username().equals(username)) {
      throw new AuthenticationException(OWN_TRAINER_PROFILE_REQUIRED);
    }

    return gymFacade.getTrainerTrainings(
        new TrainerTrainingsRequest(
            username, fromDate, toDate, traineeName, PageRequest.firstPage()));
  }
}
