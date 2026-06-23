package com.epam.gymcrm.workload.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts application exceptions into REST responses.
 */
@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(TrainerWorkloadNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(TrainerWorkloadNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException exception) {
    return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
  }
}
