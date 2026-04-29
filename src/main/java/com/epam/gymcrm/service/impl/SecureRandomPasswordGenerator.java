package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.service.PasswordGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomPasswordGenerator implements PasswordGenerator {

    private final SecureRandom random;
    private final int passwordLength;
    private final String passwordAlphabet;

    public SecureRandomPasswordGenerator(
            SecureRandom random,
            @Value("${password.length}") int passwordLength,
            @Value("${password.alphabet}") String passwordAlphabet
    ) {
        if (passwordLength <= 0) {
            throw new IllegalArgumentException("Password length must be positive");
        }
        if (passwordAlphabet == null || passwordAlphabet.isEmpty()) {
            throw new IllegalArgumentException("Password alphabet must not be empty");
        }
        this.random = random;
        this.passwordLength = passwordLength;
        this.passwordAlphabet = passwordAlphabet;
    }

    @Override
    public String generate() {
        StringBuilder password = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            password.append(passwordAlphabet.charAt(random.nextInt(passwordAlphabet.length())));
        }
        return password.toString();
    }
}
