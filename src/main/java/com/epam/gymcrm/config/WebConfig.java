package com.epam.gymcrm.config;

import com.epam.gymcrm.web.logging.RestLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final RestLoggingInterceptor restLoggingInterceptor;

  @Autowired
  public WebConfig(RestLoggingInterceptor restLoggingInterceptor) {
    this.restLoggingInterceptor = restLoggingInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(restLoggingInterceptor);
  }
}
