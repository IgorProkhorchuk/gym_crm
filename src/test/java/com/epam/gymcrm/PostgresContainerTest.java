package com.epam.gymcrm;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Main.class)
@Transactional
public abstract class PostgresContainerTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("gym_crm_test")
            .withUsername("gym_user")
            .withPassword("password");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("db.url", POSTGRES::getJdbcUrl);
        registry.add("db.username", POSTGRES::getUsername);
        registry.add("db.password", POSTGRES::getPassword);
        registry.add("db.driver", POSTGRES::getDriverClassName);
    }
}
