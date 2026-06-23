package com.epam.gymcrm.workload.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts application exceptions into REST responses.
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(TrainerWorkloadNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(TrainerWorkloadNotFoundException exception) {
    log.warn(
        "Trainer workload request failed, status={}, message={}",
        HttpStatus.NOT_FOUND,
        exception.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException exception) {
    log.warn(
        "Trainer workload request failed, status={}, message={}",
        HttpStatus.BAD_REQUEST,
        exception.getMessage());
    return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
  }
}
