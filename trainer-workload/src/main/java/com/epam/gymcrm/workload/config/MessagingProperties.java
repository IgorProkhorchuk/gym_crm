package com.epam.gymcrm.workload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trainer-workload.messaging")
public record MessagingProperties(
    String queue,
    String deadLetterQueue,
    Listener listener,
    Redelivery redelivery
) {

  public record Listener(String concurrency) {}

  public record Redelivery(
      int maximumRedeliveries,
      long initialRedeliveryDelay,
      boolean useExponentialBackOff,
      double backOffMultiplier
  ) {}
}
