package com.epam.gymcrm.service;

/**
 * Generates passwords for newly created user profiles.
 */
public interface PasswordGenerator {

    /**
     * Creates a password that satisfies the configured password policy.
     *
     * @return generated password value
     */
    String generate();
}
