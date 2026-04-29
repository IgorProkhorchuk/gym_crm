package com.epam.gymcrm.service.impl;

import com.epam.gymcrm.service.UsernameGenerator;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UsernameGeneratorImpl implements UsernameGenerator {
    @Override
    public String generate(String firstName, String lastName, Set<String> existingUsernames) {
        String baseUsername = firstName + "." + lastName;

        if (!existingUsernames.contains(baseUsername)) {
            return baseUsername;
        }
        int suffix = 1;
        while (existingUsernames.contains(baseUsername + suffix)) {
            suffix++;
        }
        return baseUsername + suffix;
    }
}
