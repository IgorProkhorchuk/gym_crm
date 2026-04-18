package com.epam.gymcrm.storage;

import com.epam.gymcrm.Main;
import com.epam.gymcrm.model.Trainee;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class StorageIntegrationTest {

    @Test
    void testStorageInitializesFromFile() {
        ApplicationContext context = new AnnotationConfigApplicationContext(Main.class);
        InMemoryStorage storage = context.getBean(InMemoryStorage.class);

        assertFalse(storage.getStorage(Trainee.class).isEmpty(),
                "Storage should be initialized with data from JSON file");
    }
}