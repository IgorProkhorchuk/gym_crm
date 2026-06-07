package com.epam.gymcrm.repository;

import static com.epam.gymcrm.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.model.User;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserRepositoryTest extends PostgresContainerTest {

  @PersistenceContext private EntityManager entityManager;

  @Resource private UserRepository userRepository;

  @Test
  void saveShouldPersistUserAndFindByUsernameShouldReturnIt() {
    User user = user("John", "Doe", "John.Doe");

    userRepository.save(user);
    entityManager.flush();
    entityManager.clear();

    Optional<User> found = userRepository.findByUsername("John.Doe");

    assertAll(
        () -> assertThat(found).isPresent(),
        () -> assertThat(found.get().getFirstName()).isEqualTo("John"),
        () -> assertThat(found.get().getLastName()).isEqualTo("Doe"),
        () -> assertThat(found.get().getUsername()).isEqualTo("John.Doe"));
  }

  @Test
  void saveShouldMergeExistingUser() {
    User user = user("Jane", "Doe", "Jane.Doe");
    entityManager.persist(user);
    entityManager.flush();
    entityManager.clear();

    user.setLastName("Updated");
    userRepository.save(user);
    entityManager.flush();
    entityManager.clear();

    Optional<User> found = userRepository.findByUsername("Jane.Doe");

    assertThat(found).isPresent().get().extracting(User::getLastName).isEqualTo("Updated");
  }

  @Test
  void findByUsernameShouldReturnEmptyOptionalWhenUserDoesNotExist() {
    Optional<User> found = userRepository.findByUsername("Unknown.User");

    assertThat(found).isEmpty();
  }

  @Test
  void findUsernamesByPatternShouldReturnMatchingUsernamesFromAllUsers() {
    entityManager.persist(user("John", "Doe", "John.Doe"));
    entityManager.persist(user("John", "Doe", "John.Doe1"));
    entityManager.persist(user("John", "Doe", "John.Doering"));
    entityManager.persist(user("Jane", "Doe", "Jane.Doe"));
    entityManager.flush();
    entityManager.clear();

    Set<String> usernames = userRepository.findUsernamesByPattern("John.Doe%");

    assertThat(usernames).containsExactlyInAnyOrder("John.Doe", "John.Doe1", "John.Doering");
  }
}
