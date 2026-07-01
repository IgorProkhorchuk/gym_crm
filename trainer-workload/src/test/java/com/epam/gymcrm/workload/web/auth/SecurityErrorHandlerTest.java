package com.epam.gymcrm.workload.web.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

class SecurityErrorHandlerTest {

  private final SecurityErrorHandler handler = new SecurityErrorHandler();

  @Test
  void commenceShouldReturnUnauthorizedForMissingAuthentication() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    AuthenticationException exception = new AuthenticationException("Missing token") {};

    handler.commence(new MockHttpServletRequest(), response, exception);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).isEqualTo("application/json");
    assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"Authentication is required\"}");
  }

  @Test
  void commenceShouldReturnOauthDescriptionWhenTokenIsInvalid() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    OAuth2Error error = new OAuth2Error("invalid_token", "Token expired", null);

    handler.commence(
        new MockHttpServletRequest(),
        response,
        new OAuth2AuthenticationException(error));

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"Token expired\"}");
  }

  @Test
  void handleShouldReturnForbiddenForDeniedAccess() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.handle(
        new MockHttpServletRequest(), response, new AccessDeniedException("Forbidden"));

    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"Access is denied\"}");
  }
}
