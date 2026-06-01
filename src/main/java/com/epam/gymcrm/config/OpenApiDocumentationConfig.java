package com.epam.gymcrm.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  SpringDocConfiguration.class,
  SpringDocWebMvcConfiguration.class,
  SwaggerConfig.class,
  SpringDocConfigProperties.class,
  SwaggerUiConfigProperties.class,
  SwaggerUiOAuthProperties.class,
  JacksonAutoConfiguration.class
})
@OpenAPIDefinition(
    info =
        @Info(
            title = "Gym CRM REST API",
            version = "1.0",
            description = "REST API for trainee, trainer, training, and training type management."),
    servers = @Server(url = "/api", description = "API servlet path"))
@SecurityScheme(
    name = "tokenAuth",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "X-Auth-Token")
public class OpenApiDocumentationConfig {

  @Bean
  public WebProperties webProperties() {
    return new WebProperties();
  }

  @Bean
  public WebMvcProperties webMvcProperties() {
    return new WebMvcProperties();
  }
}
