package com.epam.gymcrm.workload.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ConditionalOnProperty(
    name = "spring.data.mongodb.repositories.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class MongoConfig {

  @Bean
  @ConditionalOnMissingBean
  public MongoTransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
    return new MongoTransactionManager(mongoDatabaseFactory);
  }

  @Bean
  @ConditionalOnMissingBean
  public TransactionTemplate transactionTemplate(MongoTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
  }
}
