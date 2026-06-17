package com.epam.gymcrm.repository;

import com.epam.gymcrm.model.User;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repository contract for {@link User} records. */
public interface UserRepository extends JpaRepository<User, Long> {
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
  @Query("select u.username from User u where u.username like :pattern")
  Set<String> findUsernamesByPattern(String pattern);
}
