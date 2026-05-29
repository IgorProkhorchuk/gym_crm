package com.epam.gymcrm.web.logging;

import com.epam.gymcrm.logging.AuditContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RestLoggingInterceptor implements HandlerInterceptor {

  static final String START_TIME_ATTRIBUTE =
      RestLoggingInterceptor.class.getName() + ".startTime";
  private static final String PROTECTED_VALUE = "$1=[PROTECTED]";
  private static final String SENSITIVE_VALUE_PATTERN = "(?i)(password|token|secret)=\\S+";
  private static final long NANOS_PER_MILLI = 1_000_000L;
  private static final Map<String, String> OPERATION_NAMES =
      Map.ofEntries(
          Map.entry("POST /v1/auth/login", "LOGIN"),
          Map.entry("POST /v1/trainees", "CREATE_TRAINEE_PROFILE"),
          Map.entry("GET /v1/trainees/profile", "GET_TRAINEE_PROFILE"),
          Map.entry("PUT /v1/trainees/profile", "UPDATE_TRAINEE_PROFILE"),
          Map.entry("DELETE /v1/trainees/profile", "DELETE_TRAINEE_PROFILE"),
          Map.entry("PUT /v1/trainees/password", "CHANGE_TRAINEE_PASSWORD"),
          Map.entry("PATCH /v1/trainees/profile/status", "SWITCH_TRAINEE_STATUS"),
          Map.entry("GET /v1/trainees/trainers/unassigned", "GET_UNASSIGNED_TRAINERS"),
          Map.entry("PUT /v1/trainees/trainers", "UPDATE_TRAINEE_TRAINERS"),
          Map.entry("GET /v1/trainees/trainings", "GET_TRAINEE_TRAININGS"),
          Map.entry("POST /v1/trainers", "CREATE_TRAINER_PROFILE"),
          Map.entry("GET /v1/trainers/profile", "GET_TRAINER_PROFILE"),
          Map.entry("PUT /v1/trainers/profile", "UPDATE_TRAINER_PROFILE"),
          Map.entry("PUT /v1/trainers/password", "CHANGE_TRAINER_PASSWORD"),
          Map.entry("PATCH /v1/trainers/profile/status", "SWITCH_TRAINER_STATUS"),
          Map.entry("GET /v1/trainers/trainings", "GET_TRAINER_TRAININGS"),
          Map.entry("POST /v1/trainings", "ADD_TRAINING"),
          Map.entry("GET /v1/training-types", "GET_TRAINING_TYPES"));

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String operationName = resolveOperationName(request.getMethod(), request.getRequestURI());
    AuditContext.setTransactionId(UUID.randomUUID().toString());
    AuditContext.setOperationName(operationName);
    request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
    log.info(
        "REST request started operation={} method={} path={}",
        operationName,
        request.getMethod(),
        request.getRequestURI());
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      Exception exception) {
    long durationMillis = durationMillis(request);

    if (exception == null) {
      logCompletion(request, response, durationMillis);
    } else {
      log.warn(
          "REST request failed operation={} method={} path={} status={} durationMs={} "
              + "profileType={} userId={} profileId={} trainingId={} errorType={} errorMessage={}",
          AuditContext.get(AuditContext.OPERATION_NAME),
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          durationMillis,
          AuditContext.get(AuditContext.PROFILE_TYPE),
          AuditContext.get(AuditContext.USER_ID),
          AuditContext.get(AuditContext.PROFILE_ID),
          AuditContext.get(AuditContext.TRAINING_ID),
          exception.getClass().getSimpleName(),
          sanitize(exception.getMessage()));
    }
    AuditContext.clear();
  }

  private static String sanitize(String message) {
    return String.valueOf(message).replaceAll(SENSITIVE_VALUE_PATTERN, PROTECTED_VALUE);
  }

  private static long durationMillis(HttpServletRequest request) {
    Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
    if (!(startTime instanceof Long startNanos)) {
      return 0L;
    }
    return (System.nanoTime() - startNanos) / NANOS_PER_MILLI;
  }

  private static void logCompletion(
      HttpServletRequest request, HttpServletResponse response, long durationMillis) {
    if (response.getStatus() >= HttpServletResponse.SC_BAD_REQUEST) {
      log.warn(
          "REST request completed operation={} method={} path={} status={} durationMs={} "
              + "profileType={} userId={} profileId={} trainingId={}",
          AuditContext.get(AuditContext.OPERATION_NAME),
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          durationMillis,
          AuditContext.get(AuditContext.PROFILE_TYPE),
          AuditContext.get(AuditContext.USER_ID),
          AuditContext.get(AuditContext.PROFILE_ID),
          AuditContext.get(AuditContext.TRAINING_ID));
      return;
    }
    log.info(
        "REST request completed operation={} method={} path={} status={} durationMs={} "
            + "profileType={} userId={} profileId={} trainingId={}",
        AuditContext.get(AuditContext.OPERATION_NAME),
        request.getMethod(),
        request.getRequestURI(),
        response.getStatus(),
        durationMillis,
        AuditContext.get(AuditContext.PROFILE_TYPE),
        AuditContext.get(AuditContext.USER_ID),
        AuditContext.get(AuditContext.PROFILE_ID),
        AuditContext.get(AuditContext.TRAINING_ID));
  }

  private static String resolveOperationName(String method, String path) {
    return OPERATION_NAMES.getOrDefault(method + " " + apiPath(path), "UNKNOWN_OPERATION");
  }

  private static String apiPath(String path) {
    int apiPathStart = path.indexOf("/v1/");
    if (apiPathStart < 0) {
      return path;
    }
    return path.substring(apiPathStart);
  }
}
