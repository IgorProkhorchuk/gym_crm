package com.epam.gymcrm.config;

import java.security.SecureRandom;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecureConfig {
  @Bean
  public SecureRandom secureRandom() {
    return new SecureRandom();
  }

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
