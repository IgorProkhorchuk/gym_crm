package com.epam.gymcrm.web.controller;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.dto.training.AddTrainingRequest;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.web.api.TrainingApi;
import com.epam.gymcrm.web.auth.AuthenticatedPrincipal;
import com.epam.gymcrm.web.auth.AuthenticatedUserProvider;
import com.epam.gymcrm.web.dto.AddTrainingRestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/trainings")
@RequiredArgsConstructor
public class TrainingController implements TrainingApi {

  private static final String TRAINEE_PROFILE_REQUIRED =
      "This operation is available only for trainee profiles";
  private static final String OWN_TRAINEE_PROFILE_REQUIRED =
      "Authenticated trainee can add trainings only for own profile";

  private final GymFacade gymFacade;
  private final AuthenticatedUserProvider authenticatedUserProvider;

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  @Override
  public void addTraining(@Valid @RequestBody AddTrainingRestRequest request) {
    AuthenticatedPrincipal user = authenticatedUserProvider.currentUser();
    if (user.profileType() != ProfileType.TRAINEE) {
      throw new AuthenticationException(TRAINEE_PROFILE_REQUIRED);
    }
    if (!user.username().equals(request.traineeUsername())) {
      throw new AuthenticationException(OWN_TRAINEE_PROFILE_REQUIRED);
    }

    gymFacade.addTraining(
        new AddTrainingRequest(
            request.traineeUsername(),
            request.trainerUsername(),
            request.trainingName(),
            request.trainingTypeName(),
            request.trainingDate(),
            request.trainingDuration()));
  }

  @DeleteMapping("/{trainingId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Override
  public void deleteTraining(@PathVariable Long trainingId) {
    authenticatedUserProvider.currentUser();
    gymFacade.deleteTraining(trainingId);
  }
}
