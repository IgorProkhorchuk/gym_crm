package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.PostgresContainerTest;
import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.model.User;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static com.epam.gymcrm.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserDaoImplTest extends PostgresContainerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private UserDao userDao;

    @Test
    void saveShouldPersistUserAndFindByUsernameShouldReturnIt() {
        User user = user("John", "Doe", "John.Doe");

        userDao.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userDao.findByUsername("John.Doe");

        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getFirstName()).isEqualTo("John"),
                () -> assertThat(found.get().getLastName()).isEqualTo("Doe"),
                () -> assertThat(found.get().getUsername()).isEqualTo("John.Doe")
        );
    }

    @Test
    void saveShouldMergeExistingUser() {
        User user = user("Jane", "Doe", "Jane.Doe");
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        user.setLastName("Updated");
        userDao.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userDao.findByUsername("Jane.Doe");

        assertThat(found)
                .isPresent()
                .get()
                .extracting(User::getLastName)
                .isEqualTo("Updated");
    }

    @Test
    void findByUsernameShouldReturnEmptyOptionalWhenUserDoesNotExist() {
        Optional<User> found = userDao.findByUsername("Unknown.User");

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

        Set<String> usernames = userDao.findUsernamesByPattern("John.Doe%");

        assertThat(usernames).containsExactlyInAnyOrder("John.Doe", "John.Doe1", "John.Doering");
    }
}
