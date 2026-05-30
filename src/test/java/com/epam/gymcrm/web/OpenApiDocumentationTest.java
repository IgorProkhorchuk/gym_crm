package com.epam.gymcrm.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.gymcrm.config.WebConfig;
import com.epam.gymcrm.facade.GymFacade;
import com.epam.gymcrm.service.AuthenticationService;
import com.epam.gymcrm.web.auth.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringJUnitWebConfig
@ContextConfiguration(classes = {WebConfig.class, OpenApiDocumentationTest.TestConfig.class})
class OpenApiDocumentationTest {

  private final MockMvc mockMvc;

  OpenApiDocumentationTest(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void apiDocsShouldExposeOpenApiContract() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("\"title\":\"Gym CRM REST API\"")))
        .andExpect(content().string(containsString("\"name\":\"X-Auth-Token\"")));
  }

  @Test
  void swaggerUiShouldRedirectToIndexPage() throws Exception {
    mockMvc
        .perform(get("/swagger-ui.html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("/swagger-ui/index.html")));
  }

  @Configuration
  static class TestConfig {

    @Bean
    GymFacade gymFacade() {
      return mock(GymFacade.class);
    }

    @Bean
    @Primary
    TokenService tokenService() {
      return mock(TokenService.class);
    }

    @Bean
    AuthenticationService authenticationService() {
      return mock(AuthenticationService.class);
    }
  }
}
