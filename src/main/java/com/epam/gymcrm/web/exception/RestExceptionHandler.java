package com.epam.gymcrm.web.exception;

import com.epam.gymcrm.exception.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception) {
    return new ErrorResponse(exception.getMessage());
  }

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorResponse handleAuthenticationException(AuthenticationException exception) {
    return new ErrorResponse(exception.getMessage());
  }
}
