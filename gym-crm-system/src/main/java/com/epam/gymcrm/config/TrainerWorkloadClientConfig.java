package com.epam.gymcrm.config;

import com.epam.gymcrm.client.workload.ServiceJwtTokenProvider;
import com.epam.gymcrm.web.logging.RestLoggingInterceptor;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configures trainer workload service client infrastructure.
 */
@Configuration
public class TrainerWorkloadClientConfig {

  @Bean
  @LoadBalanced
  public RestTemplate trainerWorkloadRestTemplate(ServiceJwtTokenProvider tokenProvider) {
    RestTemplate restTemplate = new RestTemplate();
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

  private static String resolveTransactionId() {
    String transactionId = MDC.get(RestLoggingInterceptor.TRANSACTION_ID);
    if (transactionId == null || transactionId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return transactionId;
  }
}
