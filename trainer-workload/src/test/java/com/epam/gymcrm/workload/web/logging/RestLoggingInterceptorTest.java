package com.epam.gymcrm.workload.web.logging;

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
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/trainer-workloads");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, HANDLER);

    assertThat(result).isTrue();
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNotBlank();
    assertThat(response.getHeader(RestLoggingInterceptor.TRANSACTION_ID_HEADER))
        .isEqualTo(MDC.get(RestLoggingInterceptor.TRANSACTION_ID));
    assertThat(request.getAttribute(RestLoggingInterceptor.START_TIME_ATTRIBUTE))
        .isInstanceOf(Long.class);
  }

  @Test
  void preHandleShouldUseIncomingTransactionId() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/trainer-workloads");
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(RestLoggingInterceptor.TRANSACTION_ID_HEADER, "existing-transaction-id");

    boolean result = interceptor.preHandle(request, response, HANDLER);

    assertThat(result).isTrue();
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isEqualTo("existing-transaction-id");
    assertThat(response.getHeader(RestLoggingInterceptor.TRANSACTION_ID_HEADER))
        .isEqualTo("existing-transaction-id");
  }

  @Test
  void preHandleShouldReplaceBlankIncomingTransactionId() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/trainer-workloads");
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(RestLoggingInterceptor.TRANSACTION_ID_HEADER, " ");

    boolean result = interceptor.preHandle(request, response, HANDLER);

    assertThat(result).isTrue();
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNotBlank();
    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNotEqualTo(" ");
    assertThat(response.getHeader(RestLoggingInterceptor.TRANSACTION_ID_HEADER))
        .isEqualTo(MDC.get(RestLoggingInterceptor.TRANSACTION_ID));
  }

  @Test
  void afterCompletionShouldClearTransactionContextForSuccessfulRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/trainer-workloads");
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(HttpServletResponseStatus.OK);
    interceptor.preHandle(request, response, HANDLER);

    interceptor.afterCompletion(request, response, HANDLER, null);

    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNull();
  }

  @Test
  void afterCompletionShouldClearTransactionContextForFailedRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/trainer-workloads");
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(HttpServletResponseStatus.SERVER_ERROR);
    interceptor.preHandle(request, response, HANDLER);

    interceptor.afterCompletion(
        request, response, HANDLER, new RuntimeException("token=secret"));

    assertThat(MDC.get(RestLoggingInterceptor.TRANSACTION_ID)).isNull();
  }

  private static final class HttpServletResponseStatus {

    private static final int OK = 200;
    private static final int SERVER_ERROR = 500;
  }
}
