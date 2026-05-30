package com.epam.gymcrm.service.validation;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireEachNonBlank;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonNull;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requirePositive;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServiceValidationUtilsTest {

  @Test
  void constructorShouldBePrivate() throws Exception {
    Constructor<ServiceValidationUtils> constructor =
        ServiceValidationUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    assertThatCode(constructor::newInstance).doesNotThrowAnyException();
  }

  @Test
  void requireNonNullShouldReturnValueWhenValueIsNotNull() {
    Object value = new Object();

    assertThat(requireNonNull(value, "Value must not be null")).isSameAs(value);
  }

  @Test
  void requireNonNullShouldThrowWhenValueIsNull() {
    assertThatThrownBy(() -> requireNonNull(null, "Value must not be null"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value must not be null");
  }

  @Test
  void requireNonBlankShouldReturnValueWhenValueHasText() {
    assertThat(requireNonBlank(" text ", "Value must not be blank")).isEqualTo(" text ");
  }

  @Test
  void requireNonBlankShouldThrowWhenValueIsNull() {
    assertThatThrownBy(() -> requireNonBlank(null, "Value must not be blank"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value must not be blank");
  }

  @Test
  void requireNonBlankShouldThrowWhenValueIsBlank() {
    assertThatThrownBy(() -> requireNonBlank(" ", "Value must not be blank"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value must not be blank");
  }

  @Test
  void requirePositiveShouldReturnValueWhenValueIsPositive() {
    assertThat(requirePositive(1, "Value must be positive")).isEqualTo(1);
  }

  @Test
  void requirePositiveShouldThrowWhenValueIsNull() {
    assertThatThrownBy(() -> requirePositive(null, "Value must be positive"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value must be positive");
  }

  @Test
  void requirePositiveShouldThrowWhenValueIsZero() {
    assertThatThrownBy(() -> requirePositive(0, "Value must be positive"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value must be positive");
  }

  @Test
  void requireEachNonBlankShouldPassWhenAllValuesHaveText() {
    requireEachNonBlank(List.of("first", "second"), "Value must not be blank");
  }

  @Test
  void requireEachNonBlankShouldThrowWhenValueIsNull() {
    assertThatThrownBy(
            () -> requireEachNonBlank(Collections.singletonList(null), "Value must not be blank"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value must not be blank");
  }

  @Test
  void requireEachNonBlankShouldThrowWhenValueIsBlank() {
    assertThatThrownBy(() -> requireEachNonBlank(List.of("first", " "), "Value must not be blank"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value must not be blank");
  }
}
