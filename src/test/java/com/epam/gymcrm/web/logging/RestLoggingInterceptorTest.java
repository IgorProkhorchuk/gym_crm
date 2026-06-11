package com.epam.gymcrm.web.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RestLoggingInterceptorTest {

  private static final Object HANDLER = new Object();

  private final RestLoggingInterceptor interceptor = new RestLoggingInterceptor();

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void preHandleShouldCreateTransactionContext() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/trainees/profile");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, HANDLER);

    assertThat(result).isTrue();
    assertThat(MDC.get("transactionId")).isNotBlank();
    assertThat(request.getAttribute(RestLoggingInterceptor.START_TIME_ATTRIBUTE)).isInstanceOf(Long.class);
  }

  @Test
  void afterCompletionShouldClearTransactionContextForSuccessfulRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/trainees/profile");
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(HttpServletResponseStatus.OK);
    interceptor.preHandle(request, response, HANDLER);

    interceptor.afterCompletion(request, response, HANDLER, null);

    assertThat(MDC.get("transactionId")).isNull();
  }

  @Test
  void afterCompletionShouldClearTransactionContextForFailedRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/v1/trainers/password");
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(HttpServletResponseStatus.SERVER_ERROR);
    interceptor.preHandle(request, response, HANDLER);

    interceptor.afterCompletion(request, response, HANDLER, new RuntimeException("password=secret"));

    assertThat(MDC.get("transactionId")).isNull();
  }

  private static final class HttpServletResponseStatus {

    private static final int OK = 200;
    private static final int SERVER_ERROR = 500;
  }
}
