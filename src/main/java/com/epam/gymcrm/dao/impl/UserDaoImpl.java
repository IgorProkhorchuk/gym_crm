package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Dao
public class UserDaoImpl implements UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> findByUsername(String username) {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Set<String> findUsernamesByPattern(String pattern) {
        return new HashSet<>(entityManager.createQuery(
                "SELECT u.username FROM User u WHERE u.username LIKE :pattern", String.class)
                .setParameter("pattern", pattern)
                .getResultList());
    }

    @Override
    public void save(User user) {
        if (user.getUserId() != null) {
            entityManager.merge(user);
        } else {
            entityManager.persist(user);
        }
    }
}
