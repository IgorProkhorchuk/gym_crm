package com.epam.gymcrm.service;

import java.util.Set;

public interface UsernameGenerator {
    String generate(String firstName, String lastName, Set<String> existingUsernames);
}
