package com.epam.gymcrm.web.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.gymcrm.exception.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
                .string(
                    containsString(
                        "Required request parameter 'username' for method parameter type String")));
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

    @PostMapping("/body")
    String body(@Valid @RequestBody TestRequest request) {
      return request.value();
    }

    @GetMapping("/unexpected")
    String unexpected() {
      throw new RuntimeException("password=secret");
    }
  }

  private record TestRequest(@NotBlank(message = "must not be blank") String value) {}
}
