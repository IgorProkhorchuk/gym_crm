package com.epam.gymcrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(
    basePackages = "com.epam.gymcrm",
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.epam\\.gymcrm\\.web\\..*"),
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
    })
@PropertySource("classpath:application.properties")
@PropertySource(value = "file:${user.dir}/.env", ignoreResourceNotFound = true)
public class RootConfig {
  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
