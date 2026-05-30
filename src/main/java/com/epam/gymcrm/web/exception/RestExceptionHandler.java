package com.epam.gymcrm.web.exception;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.exception.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

  private static final String MALFORMED_REQUEST_BODY = "Malformed request body";
  private static final String INVALID_REQUEST_BODY = "Invalid request body";
  private static final String UNEXPECTED_ERROR = "Unexpected server error";

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception) {
    return new ErrorResponse(exception.getMessage());
  }

  @ExceptionHandler({
    MissingRequestHeaderException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleBadRequest(Exception exception) {
    return new ErrorResponse(exception.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleHttpMessageNotReadableException() {
    return new ErrorResponse(MALFORMED_REQUEST_BODY + ": request body is missing or invalid JSON");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception) {
    String validationErrors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(RestExceptionHandler::formatFieldError)
            .collect(Collectors.joining("; "));
    return new ErrorResponse(INVALID_REQUEST_BODY + ": " + validationErrors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
    String validationErrors =
        exception.getConstraintViolations().stream()
            .map(
                violation ->
                    "field '"
                        + violation.getPropertyPath()
                        + "' "
                        + violation.getMessage())
            .collect(Collectors.joining("; "));
    return new ErrorResponse(INVALID_REQUEST_BODY + ": " + validationErrors);
  }

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorResponse handleAuthenticationException(AuthenticationException exception) {
    return new ErrorResponse(exception.getMessage());
  }

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleEntityNotFoundException(EntityNotFoundException exception) {
    return new ErrorResponse(exception.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleUnexpectedException(Exception exception) {
    log.error("Unexpected REST error type={}", exception.getClass().getSimpleName());
    return new ErrorResponse(UNEXPECTED_ERROR);
  }

  private static String formatFieldError(FieldError error) {
    return "field '" + error.getField() + "' " + error.getDefaultMessage();
  }
}
