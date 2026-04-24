package com.epam.gymcrm.service;

import com.epam.gymcrm.service.impl.SecureRandomPasswordGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecureRandomPasswordGeneratorTest {

    @Test
    void generateReturnsConfiguredLengthAndAlphabet() {
        PasswordGenerator passwordGenerator = new SecureRandomPasswordGenerator(12, "AB");

        String password = passwordGenerator.generate();

        assertEquals(12, password.length());
        assertTrue(password.chars().allMatch(character -> character == 'A' || character == 'B'));
    }

    @Test
    void constructorRejectsNonPositiveLength() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SecureRandomPasswordGenerator(0, "ABC")
        );

        assertEquals("Password length must be positive", exception.getMessage());
    }

    @Test
    void constructorRejectsEmptyAlphabet() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SecureRandomPasswordGenerator(10, "")
        );

        assertEquals("Password alphabet must not be empty", exception.getMessage());
    }
}
