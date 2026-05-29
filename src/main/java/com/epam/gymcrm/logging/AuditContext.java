package com.epam.gymcrm.logging;

import com.epam.gymcrm.dto.auth.ProfileType;
import org.slf4j.MDC;

/** Stores safe audit fields in the logging MDC for the current request. */
public final class AuditContext {

  public static final String TRANSACTION_ID = "transactionId";
  public static final String OPERATION_NAME = "operationName";
  public static final String PROFILE_TYPE = "profileType";
  public static final String USER_ID = "userId";
  public static final String PROFILE_ID = "profileId";
  public static final String TRAINING_ID = "trainingId";

  private AuditContext() {}

  /** Sets the request transaction id. */
  public static void setTransactionId(String transactionId) {
    put(TRANSACTION_ID, transactionId);
  }

  /** Sets the REST operation name. */
  public static void setOperationName(String operationName) {
    put(OPERATION_NAME, operationName);
  }

  /** Sets the authenticated user's safe business identifiers. */
  public static void setAuthenticatedUser(
      ProfileType profileType, Long userId, Long profileId) {
    if (!hasTransaction()) {
      return;
    }
    put(PROFILE_TYPE, profileType);
    put(USER_ID, userId);
    put(PROFILE_ID, profileId);
  }

  /** Sets the created or processed training id. */
  public static void setTrainingId(Long trainingId) {
    if (!hasTransaction()) {
      return;
    }
    put(TRAINING_ID, trainingId);
  }

  /** Returns the current value for the MDC key. */
  public static String get(String key) {
    return MDC.get(key);
  }

  /** Clears all audit fields owned by the application. */
  public static void clear() {
    MDC.remove(TRANSACTION_ID);
    MDC.remove(OPERATION_NAME);
    MDC.remove(PROFILE_TYPE);
    MDC.remove(USER_ID);
    MDC.remove(PROFILE_ID);
    MDC.remove(TRAINING_ID);
  }

  private static void put(String key, Object value) {
    if (value == null) {
      MDC.remove(key);
      return;
    }
    MDC.put(key, String.valueOf(value));
  }

  private static boolean hasTransaction() {
    return MDC.get(TRANSACTION_ID) != null;
  }
}
