package com.epam.gymcrm.config;

import com.epam.gymcrm.client.workload.ServiceJwtTokenProvider;
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
              return execution.execute(request, body);
            });
    return restTemplate;
  }
}
