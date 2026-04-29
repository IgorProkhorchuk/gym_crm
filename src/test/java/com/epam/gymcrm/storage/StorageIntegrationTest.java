package com.epam.gymcrm.storage;

import com.epam.gymcrm.Main;
import com.epam.gymcrm.model.Trainee;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class StorageIntegrationTest {

    @Test
    void storageShouldInitializeFromFileWhenApplicationContextStarts() {
        ApplicationContext context = new AnnotationConfigApplicationContext(Main.class);
        InMemoryStorage storage = context.getBean(InMemoryStorage.class);

        assertThat(storage.getStorage(Trainee.class))
                .as("Storage should be initialized with data from JSON file")
                .isNotEmpty();
    }
}
