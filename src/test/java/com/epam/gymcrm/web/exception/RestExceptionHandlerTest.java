package com.epam.gymcrm.web.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.gymcrm.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class RestExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new RestExceptionHandler())
            .build();
  }

  @Test
  void shouldReturnNotFoundForEntityNotFoundException() throws Exception {
    mockMvc
        .perform(get("/handler-test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("\"message\":\"Trainer profile not found\"")));
  }

  @Test
  void shouldReturnBadRequestForMissingRequestParameter() throws Exception {
    mockMvc
        .perform(get("/handler-test/missing-param"))
        .andExpect(status().isBadRequest())
        .andExpect(
            content()
                .string(containsString("\"message\":\"Missing required request parameter ")));
  }

  @Test
  void shouldReturnBadRequestForMissingRequestHeader() throws Exception {
    mockMvc
        .perform(get("/handler-test/missing-header"))
        .andExpect(status().isBadRequest())
        .andExpect(
            content()
                .string(containsString("\"message\":\"Missing required request header ")));
  }

  @Test
  void shouldReturnBadRequestForInvalidDateRequestParameter() throws Exception {
    mockMvc
        .perform(get("/handler-test/date-param").queryParam("fromDate", "wrong-date"))
        .andExpect(status().isBadRequest())
        .andExpect(
            content()
                .string(
                    containsString(
                        "\"message\":\"Invalid request parameter 'fromDate': value "
                            + "'wrong-date' cannot be converted to date in yyyy-MM-dd format\"")));
  }

  @Test
  void shouldReturnBadRequestForInvalidIntegerRequestParameter() throws Exception {
    mockMvc
        .perform(get("/handler-test/integer-param").queryParam("limit", "wrong-limit"))
        .andExpect(status().isBadRequest())
        .andExpect(
            content()
                .string(
                    containsString(
                        "\"message\":\"Invalid request parameter 'limit': value "
                            + "'wrong-limit' cannot be converted to Integer\"")));
  }

  @Test
  void shouldReturnBadRequestForMalformedRequestBody() throws Exception {
    mockMvc
        .perform(
            post("/handler-test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\":"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("\"message\":\"Malformed request body:")));
  }

  @Test
  void shouldReturnBadRequestForInvalidDateRequestBodyField() {
    InvalidFormatException invalidFormat =
        InvalidFormatException.from(null, "Invalid date", "wrong-date", LocalDate.class);
    invalidFormat.prependPath(new Object(), "date");
    HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException("Invalid JSON field", invalidFormat, null);

    ErrorResponse response =
        new RestExceptionHandler().handleHttpMessageNotReadableException(exception);

    assertThat(response.message())
        .isEqualTo(
            "Malformed request body: field 'date' has invalid value. "
                + "Expected date in yyyy-MM-dd format");
  }

  @Test
  void shouldReturnBadRequestForInvalidRequestBodyValueWithoutFieldPath() {
    InvalidFormatException invalidFormat =
        InvalidFormatException.from(null, "Invalid value", "wrong-value", null);
    HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException("Invalid JSON value", invalidFormat, null);

    ErrorResponse response =
        new RestExceptionHandler().handleHttpMessageNotReadableException(exception);

    assertThat(response.message())
        .isEqualTo("Malformed request body: request body has invalid value. Expected the expected type");
  }

  @Test
  void shouldReturnBadRequestForBeanValidationErrors() throws Exception {
    mockMvc
        .perform(
            post("/handler-test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            content()
                .string(
                    containsString(
                        "\"message\":\"Invalid request body: field 'value' must not be blank\"")));
  }

  @Test
  void shouldReturnBadRequestForConstraintViolationException() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<TestRequest>> violations = validator.validate(new TestRequest(""));
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    ErrorResponse response =
        new RestExceptionHandler().handleConstraintViolationException(exception);

    assertThat(response.message())
        .isEqualTo("Invalid request body: field 'value' must not be blank");
  }

  @Test
  void shouldReturnBadRequestForIllegalArgumentException() {
    ErrorResponse response =
        new RestExceptionHandler()
            .handleIllegalArgumentException(new IllegalArgumentException("Invalid argument"));

    assertThat(response.message()).isEqualTo("Invalid argument");
  }

  @Test
  void shouldReturnInternalServerErrorForUnexpectedException() throws Exception {
    mockMvc
        .perform(get("/handler-test/unexpected"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("\"message\":\"Unexpected server error\"")));
  }

  @RestController
  @RequestMapping("/handler-test")
  private static class TestController {

    @GetMapping("/not-found")
    String notFound() {
      throw new EntityNotFoundException("Trainer profile not found");
    }

    @GetMapping("/missing-param")
    String missingParam(@RequestParam("username") String username) {
      return username;
    }

    @GetMapping("/missing-header")
    String missingHeader(@RequestHeader("X-Auth-Token") String token) {
      return token;
    }

    @GetMapping("/date-param")
    String dateParam(
        @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate) {
      return fromDate.toString();
    }

    @PostMapping("/body")
    String body(@Valid @RequestBody TestRequest request) {
      return request.value();
    }

    @GetMapping("/integer-param")
    String integerParam(@RequestParam("limit") Integer limit) {
      return limit.toString();
    }

    @GetMapping("/unexpected")
    String unexpected() {
      throw new RuntimeException("password=secret");
    }
  }

  private record TestRequest(@NotBlank(message = "must not be blank") String value) {}
}
