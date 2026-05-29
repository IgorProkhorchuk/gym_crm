package com.epam.gymcrm.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.dto.auth.ProfileType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class AuditContextTest {

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void setAuthenticatedUserShouldSkipWhenTransactionIsMissing() {
    AuditContext.setAuthenticatedUser(ProfileType.TRAINEE, 1L, 2L);

    assertThat(AuditContext.get(AuditContext.PROFILE_TYPE)).isNull();
    assertThat(AuditContext.get(AuditContext.USER_ID)).isNull();
    assertThat(AuditContext.get(AuditContext.PROFILE_ID)).isNull();
  }

  @Test
  void setAuthenticatedUserShouldStoreValuesWhenTransactionExists() {
    AuditContext.setTransactionId("tx-id");

    AuditContext.setAuthenticatedUser(ProfileType.TRAINER, 1L, 2L);

    assertThat(AuditContext.get(AuditContext.PROFILE_TYPE)).isEqualTo("TRAINER");
    assertThat(AuditContext.get(AuditContext.USER_ID)).isEqualTo("1");
    assertThat(AuditContext.get(AuditContext.PROFILE_ID)).isEqualTo("2");
  }

  @Test
  void setAuthenticatedUserShouldRemoveNullValues() {
    AuditContext.setTransactionId("tx-id");
    AuditContext.setAuthenticatedUser(ProfileType.TRAINER, 1L, 2L);

    AuditContext.setAuthenticatedUser(ProfileType.TRAINEE, null, null);

    assertThat(AuditContext.get(AuditContext.PROFILE_TYPE)).isEqualTo("TRAINEE");
    assertThat(AuditContext.get(AuditContext.USER_ID)).isNull();
    assertThat(AuditContext.get(AuditContext.PROFILE_ID)).isNull();
  }

  @Test
  void setTrainingIdShouldSkipWhenTransactionIsMissing() {
    AuditContext.setTrainingId(10L);

    assertThat(AuditContext.get(AuditContext.TRAINING_ID)).isNull();
  }

  @Test
  void setTrainingIdShouldStoreValueWhenTransactionExists() {
    AuditContext.setTransactionId("tx-id");

    AuditContext.setTrainingId(10L);

    assertThat(AuditContext.get(AuditContext.TRAINING_ID)).isEqualTo("10");
  }

  @Test
  void clearShouldRemoveAllAuditFields() {
    AuditContext.setTransactionId("tx-id");
    AuditContext.setOperationName("GET_TRAINEE_PROFILE");
    AuditContext.setAuthenticatedUser(ProfileType.TRAINEE, 1L, 2L);
    AuditContext.setTrainingId(3L);

    AuditContext.clear();

    assertThat(AuditContext.get(AuditContext.TRANSACTION_ID)).isNull();
    assertThat(AuditContext.get(AuditContext.OPERATION_NAME)).isNull();
    assertThat(AuditContext.get(AuditContext.PROFILE_TYPE)).isNull();
    assertThat(AuditContext.get(AuditContext.USER_ID)).isNull();
    assertThat(AuditContext.get(AuditContext.PROFILE_ID)).isNull();
    assertThat(AuditContext.get(AuditContext.TRAINING_ID)).isNull();
  }
}
