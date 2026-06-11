package com.epam.gymcrm.util;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.util.ReflectionUtils;

public final class SensitiveToString {

  private static final String PROTECTED_VALUE = "[PROTECTED]";

  private SensitiveToString() {}

  public static String toString(Record value) {
    return Arrays.stream(value.getClass().getRecordComponents())
        .map(component -> formatComponent(value, component))
        .collect(Collectors.joining(", ", value.getClass().getSimpleName() + "[", "]"));
  }

  private static String formatComponent(Record value, RecordComponent component) {
    Object componentValue =
        component.isAnnotationPresent(SensitiveInfo.class)
            ? PROTECTED_VALUE
            : readComponent(value, component);
    return component.getName() + "=" + componentValue;
  }

  private static Object readComponent(Record value, RecordComponent component) {
    ReflectionUtils.makeAccessible(component.getAccessor());
    return ReflectionUtils.invokeMethod(component.getAccessor(), value);
  }
}
