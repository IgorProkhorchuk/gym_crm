package com.epam.gymcrm.workload.web.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@Component
public class RestLoggingInterceptor implements HandlerInterceptor {

  public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
  public static final String TRANSACTION_ID = "transactionId";

  static final String START_TIME_ATTRIBUTE =
      RestLoggingInterceptor.class.getName() + ".startTime";
  private static final String PROTECTED_VALUE = "$1=[PROTECTED]";
  private static final String SENSITIVE_VALUE_PATTERN = "(?i)(password|token|secret)=\\S+";
  private static final long NANOS_PER_MILLI = 1_000_000L;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String transactionId = resolveTransactionId(request);
    MDC.put(TRANSACTION_ID, transactionId);
    response.setHeader(TRANSACTION_ID_HEADER, transactionId);
    request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
    log.info(
        "REST request started method={} path={} query={}",
        request.getMethod(),
        routePattern(request),
        sanitize(request.getQueryString()));
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
          routePattern(request),
          response.getStatus(),
          durationMillis);
    } else {
      log.warn(
          "REST request failed method={} path={} status={} durationMs={} errorType={} "
              + "errorMessage={}",
          request.getMethod(),
          routePattern(request),
          response.getStatus(),
          durationMillis,
          exception.getClass().getSimpleName(),
          sanitize(exception.getMessage()));
    }
    MDC.remove(TRANSACTION_ID);
  }

  private static String resolveTransactionId(HttpServletRequest request) {
    String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
    if (transactionId == null || transactionId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return transactionId;
  }

  private static String sanitize(String message) {
    return String.valueOf(message).replaceAll(SENSITIVE_VALUE_PATTERN, PROTECTED_VALUE);
  }

  private static String routePattern(HttpServletRequest request) {
    Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    return Objects.toString(pattern, "[unmatched]");
  }
}
