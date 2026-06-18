package com.epam.gymcrm.web.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RestLoggingInterceptor implements HandlerInterceptor {

  static final String START_TIME_ATTRIBUTE =
      RestLoggingInterceptor.class.getName() + ".startTime";
  private static final String TRANSACTION_ID = "transactionId";
  private static final String PROTECTED_VALUE = "$1=[PROTECTED]";
  private static final String SENSITIVE_VALUE_PATTERN = "(?i)(password|token|secret)=\\S+";
  private static final long NANOS_PER_MILLI = 1_000_000L;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    MDC.put(TRANSACTION_ID, UUID.randomUUID().toString());
    request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
    log.info("REST request started method={} path={}", request.getMethod(), request.getRequestURI());
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      Exception exception) {
    long durationMillis =
        (System.nanoTime() - (long) request.getAttribute(START_TIME_ATTRIBUTE)) / NANOS_PER_MILLI;

    if (exception == null) {
      log.info(
          "REST request completed method={} path={} status={} durationMs={}",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          durationMillis);
    } else {
      log.warn(
          "REST request failed method={} path={} status={} durationMs={} errorType={} errorMessage={}",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          durationMillis,
          exception.getClass().getSimpleName(),
          sanitize(exception.getMessage()));
    }
    MDC.remove(TRANSACTION_ID);
  }

  private static String sanitize(String message) {
    return String.valueOf(message).replaceAll(SENSITIVE_VALUE_PATTERN, PROTECTED_VALUE);
  }
}
