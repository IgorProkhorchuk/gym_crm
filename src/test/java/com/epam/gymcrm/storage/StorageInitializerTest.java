package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StorageInitializerTest {

    private StorageInitializer storageInitializer;
    private Resource mockResource;
    private InMemoryStorage inMemoryStorage;

    @BeforeEach
    void setUp() throws Exception {
        storageInitializer = new StorageInitializer();
        mockResource = mock(Resource.class);

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
        assertEquals(bean, result, "Має повернути той самий бін без змін");
    }

    @Test
    void testPostProcessAfterInitialization_FileDoesNotExist() {
        when(mockResource.exists()).thenReturn(false);

        Object result = storageInitializer.postProcessAfterInitialization(inMemoryStorage, "inMemoryStorage");

        assertEquals(inMemoryStorage, result);
        verify(mockResource, times(1)).exists();
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

        assertSame(inMemoryStorage, result);
        assertEquals(1, inMemoryStorage.getStorage(Trainee.class).size());
        assertEquals(1, inMemoryStorage.getStorage(Trainer.class).size());
        assertEquals(1, inMemoryStorage.getStorage(Training.class).size());
    }

    @Test
    void testPostProcessAfterInitialization_ThrowsIOException() throws Exception {
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenThrow(new IOException("Mocked IO Exception"));

        Object result = storageInitializer.postProcessAfterInitialization(inMemoryStorage, "inMemoryStorage");

        assertEquals(inMemoryStorage, result);

        assertEquals(0, inMemoryStorage.getStorage(Trainee.class).size());
    }

    @Test
    void testPostProcessAfterInitialization_NullBean() {
        Object result = storageInitializer.postProcessAfterInitialization(null, "testBean");
        assertNull(result);
    }

    @Test
    void testPostProcessAfterInitialization_NodeIsNotArray() throws Exception {
        when(mockResource.exists()).thenReturn(true);

        String jsonContent = "{" +
                "\"trainers\": {\"userId\": 2, \"firstName\": \"Jane\"}" +
                "}";

        java.io.InputStream inputStream = new java.io.ByteArrayInputStream(jsonContent.getBytes());
        when(mockResource.getInputStream()).thenReturn(inputStream);

        Object result = storageInitializer.postProcessAfterInitialization(inMemoryStorage, "inMemoryStorage");

        assertEquals(0, inMemoryStorage.getStorage(Trainer.class).size());
    }
}