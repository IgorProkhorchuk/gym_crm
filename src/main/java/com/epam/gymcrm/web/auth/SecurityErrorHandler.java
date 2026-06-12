package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.web.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

  private static final String AUTHENTICATION_REQUIRED = "Authentication is required";
  private static final String ACCESS_DENIED = "Access is denied";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    write(response, HttpStatus.UNAUTHORIZED, authenticationErrorMessage(authException));
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    write(response, HttpStatus.FORBIDDEN, ACCESS_DENIED);
  }

  private void write(HttpServletResponse response, HttpStatus status, String message)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), new ErrorResponse(message));
  }

  private static String authenticationErrorMessage(AuthenticationException authException) {
    return authException instanceof OAuth2AuthenticationException oauthException
        ? oauthException.getError().getDescription()
        : AUTHENTICATION_REQUIRED;
  }
}
