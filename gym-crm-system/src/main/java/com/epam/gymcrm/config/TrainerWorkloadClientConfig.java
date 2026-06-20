package com.epam.gymcrm.config;

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
  public RestTemplate trainerWorkloadRestTemplate() {
    return new RestTemplate();
  }
}
