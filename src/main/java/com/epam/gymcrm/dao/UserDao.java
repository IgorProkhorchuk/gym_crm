package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.User;
import java.util.Optional;
import java.util.Set;

/**
 * Persistence contract for {@link User} records.
 */
public interface UserDao {
    /**
     * Finds a user by their unique username.
     *
     * @param username the username to look up
     * @return the user if found, or empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds all usernames that match the given pattern (e.g., "firstName.lastName%").
     *
     * @param pattern the SQL LIKE pattern to match against
     * @return a set of matching usernames
     */
    Set<String> findUsernamesByPattern(String pattern);

    /**
     * Saves or updates a user.
     *
     * @param user the user to save
     */
    void save(User user);
}
