package com.epam.gymcrm.workload.config;

import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MessagingProperties.class)
public class JmsConfig {

  private final MessagingProperties messagingProperties;

  @Bean
  public RedeliveryPolicy trainerWorkloadRedeliveryPolicy() {
    MessagingProperties.Redelivery redelivery = messagingProperties.redelivery();
    RedeliveryPolicy policy = new RedeliveryPolicy();
    policy.setMaximumRedeliveries(redelivery.maximumRedeliveries());
    policy.setInitialRedeliveryDelay(redelivery.initialRedeliveryDelay());
    policy.setUseExponentialBackOff(redelivery.useExponentialBackOff());
    policy.setBackOffMultiplier(redelivery.backOffMultiplier());
    return policy;
  }

  @Bean
  public JmsListenerContainerFactory<DefaultMessageListenerContainer> jmsListenerContainerFactory(
      ConnectionFactory connectionFactory,
      RedeliveryPolicy trainerWorkloadRedeliveryPolicy,
      @Value("${spring.jms.listener.auto-startup:true}") boolean autoStartup
  ) {
    if (connectionFactory instanceof ActiveMQConnectionFactory activeMqConnectionFactory) {
      activeMqConnectionFactory.setRedeliveryPolicy(trainerWorkloadRedeliveryPolicy);
    }

    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setSessionTransacted(true);
    factory.setConcurrency(messagingProperties.listener().concurrency());
    factory.setAutoStartup(autoStartup);
    return factory;
  }
}
