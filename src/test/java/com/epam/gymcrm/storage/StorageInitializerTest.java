package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageInitializerTest {

    @InjectMocks
    private StorageInitializer storageInitializer;

    @Mock
    private Resource mockResource;

    private InMemoryStorage inMemoryStorage;

    @BeforeEach
    void setUp() throws Exception {
        inMemoryStorage = new InMemoryStorage();
        inMemoryStorage.setTrainees(new HashMap<>());
        inMemoryStorage.setTrainers(new HashMap<>());
        inMemoryStorage.setTrainings(new HashMap<>());

        Field resourceField = StorageInitializer.class.getDeclaredField("storageFile");
        resourceField.setAccessible(true);
        resourceField.set(storageInitializer, mockResource);
    }

    @Test
    void testPostProcessAfterInitialization_NotInMemoryStorage() {
        Object bean = new Object();
        Object result = storageInitializer.postProcessAfterInitialization(bean, "testBean");

        assertThat(result).isSameAs(bean);
    }

    @Test
    void testPostProcessAfterInitialization_FileDoesNotExist() {
        when(mockResource.exists()).thenReturn(false);

        Object result = storageInitializer.postProcessAfterInitialization(inMemoryStorage, "inMemoryStorage");

        assertAll(
                () -> assertThat(result).isSameAs(inMemoryStorage),
                () -> verify(mockResource).exists()
        );
    }

    @Test
    void testPostProcessAfterInitialization_Success() throws Exception {
        when(mockResource.exists()).thenReturn(true);

        String jsonContent = "{" +
                "\"trainees\": [{\"userId\": 1, \"firstName\": \"John\"}]," +
                "\"trainers\": [{\"userId\": 2, \"firstName\": \"Jane\"}]," +
                "\"trainings\": [{\"trainingId\": 3, \"trainingName\": \"Yoga\"}]" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes());
        when(mockResource.getInputStream()).thenReturn(inputStream);

        Object result = storageInitializer.postProcessAfterInitialization(inMemoryStorage, "inMemoryStorage");

        assertAll(
                () -> assertThat(result).isSameAs(inMemoryStorage),
                () -> assertThat(inMemoryStorage.getStorage(Trainee.class)).hasSize(1),
                () -> assertThat(inMemoryStorage.getStorage(Trainer.class)).hasSize(1),
                () -> assertThat(inMemoryStorage.getStorage(Training.class)).hasSize(1)
        );
    }

    @Test
    void testPostProcessAfterInitialization_ThrowsIOException() throws Exception {
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenThrow(new IOException("Mocked IO Exception"));

        Object result = storageInitializer.postProcessAfterInitialization(inMemoryStorage, "inMemoryStorage");

        assertAll(
                () -> assertThat(result).isSameAs(inMemoryStorage),
                () -> assertThat(inMemoryStorage.getStorage(Trainee.class)).isEmpty()
        );
    }

    @Test
    void testPostProcessAfterInitialization_NullBean() {
        Object result = storageInitializer.postProcessAfterInitialization(null, "testBean");

        assertThat(result).isNull();
    }

    @Test
    void testPostProcessAfterInitialization_NodeIsNotArray() throws Exception {
        when(mockResource.exists()).thenReturn(true);

        String jsonContent = "{" +
                "\"trainers\": {\"userId\": 2, \"firstName\": \"Jane\"}" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes());
        when(mockResource.getInputStream()).thenReturn(inputStream);

        storageInitializer.postProcessAfterInitialization(inMemoryStorage, "inMemoryStorage");

        assertThat(inMemoryStorage.getStorage(Trainer.class)).isEmpty();
    }
}
