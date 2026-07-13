package com.epam.gymcrm.workload.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerMapping;

class RestExceptionHandlerTest {

  private final RestExceptionHandler handler = new RestExceptionHandler();

  @Test
  void handleMissingRequestHeaderExceptionShouldReturnReadableMessage() {
    MissingRequestHeaderException exception =
        new MissingRequestHeaderException("X-Transaction-Id", (MethodParameter) null);

    ErrorResponse response = handler.handleMissingRequestHeaderException(exception);

    assertThat(response.message()).isEqualTo("Missing required request header 'X-Transaction-Id'");
  }

  @Test
  void handleMissingServletRequestParameterExceptionShouldReturnReadableMessage() {
    MissingServletRequestParameterException exception =
        new MissingServletRequestParameterException("fromDate", "LocalDate");

    ErrorResponse response = handler.handleMissingServletRequestParameterException(exception);

    assertThat(response.message()).isEqualTo("Missing required request parameter 'fromDate'");
  }

  @Test
  void handleMethodArgumentTypeMismatchExceptionShouldReturnExpectedType() {
    MethodArgumentTypeMismatchException exception =
        new MethodArgumentTypeMismatchException("abc", Integer.class, "year", null, null);

    ErrorResponse response = handler.handleMethodArgumentTypeMismatchException(exception);

    assertThat(response.message())
        .isEqualTo("Invalid request parameter 'year': value 'abc' cannot be converted to Integer");
  }

  @Test
  void handleMethodArgumentTypeMismatchExceptionShouldHandleMissingExpectedType() {
    MethodArgumentTypeMismatchException exception =
        new MethodArgumentTypeMismatchException("abc", null, "year", null, null);

    ErrorResponse response = handler.handleMethodArgumentTypeMismatchException(exception);

    assertThat(response.message())
        .isEqualTo(
            "Invalid request parameter 'year': value 'abc' cannot be converted to the expected type");
  }

  @Test
  void handleMethodArgumentNotValidExceptionShouldReturnFieldErrors() throws Exception {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new TestRequest(""), "testRequest");
    bindingResult.addError(new FieldError("testRequest", "name", "must not be blank"));
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(
            new MethodParameter(
                RestExceptionHandlerTest.class.getDeclaredMethod("validate", TestRequest.class),
                0),
            bindingResult);

    ErrorResponse response = handler.handleMethodArgumentNotValidException(exception);

    assertThat(response.message())
        .isEqualTo("Invalid request body: field 'name' must not be blank");
  }

  @Test
  void handleConstraintViolationExceptionShouldReturnViolationMessages() {
    Set<ConstraintViolation<TestRequest>> violations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(new TestRequest(""));
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    ErrorResponse response = handler.handleConstraintViolationException(exception);

    assertThat(response.message())
        .startsWith("Invalid request body: field 'name' must not be blank");
  }

  @Test
  void handleBadRequestShouldReturnExceptionMessage() {
    ErrorResponse response = handler.handleBadRequest(new IllegalArgumentException("Bad payload"));

    assertThat(response.message()).isEqualTo("Bad payload");
  }

  @Test
  void handleHttpMessageNotReadableExceptionShouldReturnInvalidJsonMessage() {
    HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException("Invalid JSON", inputMessage());

    ErrorResponse response = handler.handleHttpMessageNotReadableException(exception);

    assertThat(response.message())
        .isEqualTo("Malformed request body: request body is missing or invalid JSON");
  }

  @Test
  void handleHttpMessageNotReadableExceptionShouldReturnInvalidFieldMessage() {
    InvalidFormatException invalidFormat =
        InvalidFormatException.from(null, "Invalid date", "bad-date", LocalDate.class);
    invalidFormat.prependPath(new Object(), "trainingDate");
    HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException("Invalid JSON", invalidFormat, inputMessage());

    ErrorResponse response = handler.handleHttpMessageNotReadableException(exception);

    assertThat(response.message())
        .isEqualTo(
            "Malformed request body: field 'trainingDate' has invalid value. "
                + "Expected date in yyyy-MM-dd format");
  }

  @Test
  void handleHttpMessageNotReadableExceptionShouldReturnBodyMessageWhenFieldIsUnknown() {
    InvalidFormatException invalidFormat =
        InvalidFormatException.from(null, "Invalid value", "bad-value", String.class);
    HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException("Invalid JSON", invalidFormat, inputMessage());

    ErrorResponse response = handler.handleHttpMessageNotReadableException(exception);

    assertThat(response.message())
        .isEqualTo(
            "Malformed request body: request body has invalid value. Expected String");
  }

  @Test
  void handleUnexpectedExceptionShouldReturnGenericMessage() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn("POST");
    when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
        .thenReturn("/api/workload");

    ErrorResponse response = handler.handleUnexpectedException(new RuntimeException("Boom"), request);

    assertThat(response.message()).isEqualTo("Unexpected server error");
  }

  private static HttpInputMessage inputMessage() {
    return new MockHttpInputMessage(new byte[0]);
  }

  @SuppressWarnings("unused")
  private void validate(TestRequest request) {}

  private record TestRequest(@NotBlank(message = "must not be blank") String name) {}
}
