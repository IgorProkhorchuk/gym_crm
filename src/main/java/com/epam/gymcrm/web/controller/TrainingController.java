package com.epam.gymcrm.web.controller;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonNull;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requirePositive;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.api.TrainingApi;
import com.epam.gymcrm.web.auth.AuthenticatedUser;
import com.epam.gymcrm.web.auth.FakeTokenService;
import com.epam.gymcrm.web.dto.AddTrainingRestRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trainings")
public class TrainingController implements TrainingApi {

  private final GymFacade gymFacade;
  private final FakeTokenService fakeTokenService;

  @Autowired
  public TrainingController(GymFacade gymFacade, FakeTokenService fakeTokenService) {
    this.gymFacade = gymFacade;
    this.fakeTokenService = fakeTokenService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  @Override
  public void addTraining(
      @RequestHeader("X-Auth-Token") String token,
      @Valid @RequestBody AddTrainingRestRequest request) {
    AuthenticatedUser user = fakeTokenService.getUserByToken(token);
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException("Access denied");
    }
    validateRequest(request);
    if (!user.username().equals(request.traineeUsername())) {
      throw new AuthenticationException("Access denied");
    }

    gymFacade.addTraining(
        new AddTrainingRequest(
            request.traineeUsername(),
            user.password(),
            request.trainerUsername(),
            request.trainingName(),
            request.trainingTypeName(),
            request.trainingDate(),
            request.trainingDuration()));
  }

  private static void validateRequest(AddTrainingRestRequest request) {
    requireNonNull(request, "Training request must not be null");
    requireNonBlank(request.traineeUsername(), "Trainee username must not be blank");
    requireNonBlank(request.trainerUsername(), "Trainer username must not be blank");
    requireNonBlank(request.trainingName(), "Training name must not be blank");
    requireNonBlank(request.trainingTypeName(), "Training type must not be blank");
    requireNonNull(request.trainingDate(), "Training date must not be null");
    requirePositive(request.trainingDuration(), "Training duration must be positive");
  }
}
