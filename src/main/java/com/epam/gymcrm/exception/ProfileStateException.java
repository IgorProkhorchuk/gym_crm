package com.epam.gymcrm.exception;

/**
 * Thrown when a profile state transition cannot be performed because the profile is already in the requested state.
 */
public class ProfileStateException extends RuntimeException {

    public ProfileStateException(String message) {
        super(message);
    }
}
