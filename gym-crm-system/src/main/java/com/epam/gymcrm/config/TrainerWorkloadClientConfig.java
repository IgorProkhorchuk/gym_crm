package com.epam.gymcrm.config;

import com.epam.gymcrm.client.workload.ServiceJwtTokenProvider;
import com.epam.gymcrm.web.logging.RestLoggingInterceptor;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TrainerWorkloadClientConfig {

  @Bean
  @LoadBalanced
  public RestTemplate trainerWorkloadRestTemplate(
      ServiceJwtTokenProvider tokenProvider,
      @Value("${trainer-workload.client.connect-timeout:2s}") Duration connectTimeout,
      @Value("${trainer-workload.client.read-timeout:5s}") Duration readTimeout) {
    RestTemplate restTemplate =
        new RestTemplate(requestFactory(connectTimeout, readTimeout));
    restTemplate
        .getInterceptors()
        .add(
            (request, body, execution) -> {
              request.getHeaders().setBearerAuth(tokenProvider.createServiceToken());
              request
                  .getHeaders()
                  .set(
                      RestLoggingInterceptor.TRANSACTION_ID_HEADER,
                      resolveTransactionId());
              return execution.execute(request, body);
            });
    return restTemplate;
  }

  private static SimpleClientHttpRequestFactory requestFactory(
      Duration connectTimeout, Duration readTimeout) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(connectTimeout);
    requestFactory.setReadTimeout(readTimeout);
    return requestFactory;
  }

  private static String resolveTransactionId() {
    String transactionId = MDC.get(RestLoggingInterceptor.TRANSACTION_ID);
    if (transactionId == null || transactionId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return transactionId;
  }
}
