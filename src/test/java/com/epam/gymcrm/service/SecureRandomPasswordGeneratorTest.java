package com.epam.gymcrm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.epam.gymcrm.service.impl.SecureRandomPasswordGenerator;
import java.security.SecureRandom;
import org.junit.jupiter.api.Test;

class SecureRandomPasswordGeneratorTest {

  @Test
  void generateShouldReturnPasswordWithConfiguredLengthAndAlphabet() {
    PasswordGenerator passwordGenerator =
        new SecureRandomPasswordGenerator(new SecureRandom(), 12, "AB");

    String password = passwordGenerator.generate();

    assertAll(
        () -> assertThat(password).hasSize(12),
        () ->
            assertThat(password.chars())
                .allMatch(character -> character == 'A' || character == 'B'));
  }

  @Test
  void constructorShouldThrowIllegalArgumentExceptionWhenLengthIsNotPositive() {
    assertThatThrownBy(() -> new SecureRandomPasswordGenerator(new SecureRandom(), 0, "ABC"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password length must be positive");
  }

  @Test
  void constructorShouldThrowIllegalArgumentExceptionWhenAlphabetIsEmpty() {
    assertThatThrownBy(() -> new SecureRandomPasswordGenerator(new SecureRandom(), 10, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password alphabet must not be empty");
  }

  @Test
  void constructorShouldThrowIllegalArgumentExceptionWhenAlphabetIsNull() {
    assertThatThrownBy(() -> new SecureRandomPasswordGenerator(new SecureRandom(), 10, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Password alphabet must not be empty");
  }
}
