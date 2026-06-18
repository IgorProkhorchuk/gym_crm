package com.epam.gymcrm.service;

import java.util.Set;

/** Generates a unique username for a user profile. */
public interface UsernameGenerator {

  /** Builds a username from profile names and already existing usernames. */
  String generate(String firstName, String lastName, Set<String> existingUsernames);
}
