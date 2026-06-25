package com.epam.gymcrm.workload.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.Objects;
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

  @ExceptionHandler(TrainerWorkloadNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNotFound(TrainerWorkloadNotFoundException exception) {
    log.warn("Trainer workload request failed, status={}, message={}", HttpStatus.NOT_FOUND,
        exception.getMessage());
    return new ErrorResponse(exception.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleBadRequest(IllegalArgumentException exception) {
    log.warn("Trainer workload request failed, status={}, message={}", HttpStatus.BAD_REQUEST,
        exception.getMessage());
    return new ErrorResponse(exception.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleHttpMessageNotReadableException(
      HttpMessageNotReadableException exception) {
    InvalidFormatException invalidFormat = findInvalidFormatException(exception);
    if (invalidFormat != null) {
      return new ErrorResponse(
          MALFORMED_REQUEST_BODY
              + ": "
              + formatInvalidJsonField(invalidFormat));
    }
    return new ErrorResponse(MALFORMED_REQUEST_BODY + ": request body is missing or invalid JSON");
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleMissingRequestHeaderException(
      MissingRequestHeaderException exception) {
    return new ErrorResponse(
        "Missing required request header '" + exception.getHeaderName() + "'");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleMissingServletRequestParameterException(
      MissingServletRequestParameterException exception) {
    return new ErrorResponse(
        "Missing required request parameter '" + exception.getParameterName() + "'");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException exception) {
    return new ErrorResponse(
        "Invalid request parameter '"
            + exception.getName()
            + "': value '"
            + exception.getValue()
            + "' cannot be converted to "
            + formatExpectedType(exception.getRequiredType()));
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

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleUnexpectedException(Exception exception) {
    log.error("Unexpected REST error type={}", exception.getClass().getSimpleName(), exception);
    return new ErrorResponse(UNEXPECTED_ERROR);
  }

  private static String formatFieldError(FieldError error) {
    return "field '" + error.getField() + "' " + error.getDefaultMessage();
  }

  private static String formatInvalidJsonField(InvalidFormatException exception) {
    String fieldName =
        exception.getPath().stream()
            .map(JsonMappingException.Reference::getFieldName)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("."));
    String fieldDescription = fieldName.isBlank() ? "request body" : "field '" + fieldName + "'";
    return fieldDescription + " has invalid value. Expected "
        + formatExpectedType(exception.getTargetType());
  }

  private static String formatExpectedType(Class<?> type) {
    if (LocalDate.class.equals(type)) {
      return "date in yyyy-MM-dd format";
    }
    return type == null ? "the expected type" : type.getSimpleName();
  }

  private static InvalidFormatException findInvalidFormatException(Throwable exception) {
    Throwable current = exception;
    while (current != null) {
      if (current instanceof InvalidFormatException invalidFormat) {
        return invalidFormat;
      }
      current = current.getCause();
    }
    return null;
  }
}
