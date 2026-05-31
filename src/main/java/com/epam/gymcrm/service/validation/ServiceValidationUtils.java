package com.epam.gymcrm.service.validation;

public final class ServiceValidationUtils {

  private ServiceValidationUtils() {}

  public static <T> T requireNonNull(T value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  public static String requireNonBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

}
