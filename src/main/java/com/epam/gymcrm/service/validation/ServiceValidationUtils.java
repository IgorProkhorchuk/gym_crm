package com.epam.gymcrm.service.validation;

import java.util.List;

public final class ServiceValidationUtils {

    private ServiceValidationUtils() {
    }

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

    public static Integer requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static void requireEachNonBlank(List<String> values, String message) {
        values.forEach(value -> requireNonBlank(value, message));
    }
}
