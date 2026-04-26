package com.epam.gymcrm.service;

import com.epam.gymcrm.service.impl.SecureRandomPasswordGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SecureRandomPasswordGeneratorTest {

    @Test
    void generateReturnsConfiguredLengthAndAlphabet() {
        PasswordGenerator passwordGenerator = new SecureRandomPasswordGenerator(12, "AB");

        String password = passwordGenerator.generate();

        assertAll(
                () -> assertThat(password).hasSize(12),
                () -> assertThat(password.chars()).allMatch(character -> character == 'A' || character == 'B')
        );
    }

    @Test
    void constructorRejectsNonPositiveLength() {
        assertThatThrownBy(() -> new SecureRandomPasswordGenerator(0, "ABC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password length must be positive");
    }

    @Test
    void constructorRejectsEmptyAlphabet() {
        assertThatThrownBy(() -> new SecureRandomPasswordGenerator(10, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password alphabet must not be empty");
    }

    @Test
    void constructorRejectsNullAlphabet() {
        assertThatThrownBy(() -> new SecureRandomPasswordGenerator(10, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password alphabet must not be empty");
    }
}
