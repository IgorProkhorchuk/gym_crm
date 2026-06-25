package com.epam.gymcrm.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.epam.gymcrm.client.workload.ServiceJwtTokenProvider;
import com.epam.gymcrm.web.logging.RestLoggingInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class TrainerWorkloadClientConfigTest {

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void trainerWorkloadRestTemplateShouldAddServiceJwtAndTransactionId() {
    ServiceJwtTokenProvider tokenProvider = mock(ServiceJwtTokenProvider.class);
    when(tokenProvider.createServiceToken()).thenReturn("service-token");
    MDC.put(RestLoggingInterceptor.TRANSACTION_ID, "transaction-id");

    RestTemplate restTemplate =
        new TrainerWorkloadClientConfig().trainerWorkloadRestTemplate(tokenProvider);
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(requestTo("http://trainer-workload/api/v1/trainer-workloads"))
        .andExpect(header("Authorization", "Bearer service-token"))
        .andExpect(header(RestLoggingInterceptor.TRANSACTION_ID_HEADER, "transaction-id"))
        .andRespond(withSuccess());

    restTemplate.exchange(
        "http://trainer-workload/api/v1/trainer-workloads", HttpMethod.POST, null, Void.class);

    server.verify();
  }
}
