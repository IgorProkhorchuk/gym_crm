package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Component
public class StorageInitializer implements BeanPostProcessor {

    @Value("classpath:${storage.file.path}")
    private Resource storageFile;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof InMemoryStorage storage) {
            if (!storageFile.exists()) {
                log.warn("Storage file {} does not exist. Skipping pre-population.", storageFile.getFilename());
                return bean;
            }

            try (InputStream inputStream = storageFile.getInputStream()) {
                JsonNode root = mapper.readTree(inputStream);

                loadEntities(root.get("trainers"), storage.getStorage(Trainer.class), Trainer.class);
                loadEntities(root.get("trainees"), storage.getStorage(Trainee.class), Trainee.class);
                loadEntities(root.get("trainings"), storage.getStorage(Training.class), Training.class);

                log.info("Storage pre-populated from file: {}", storageFile.getFilename());
            } catch (IOException e) {
                log.error("Failed to initialize storage: {}", e.getMessage());
            }
        }
        return bean;
    }

    private <T> void loadEntities(JsonNode node, Map<Long, T> targetMap, Class<T> type) throws IOException {
        if (node != null && node.isArray()) {
            for (JsonNode n : node) {
                T entity = mapper.treeToValue(n, type);
                if (entity instanceof User u) targetMap.put(u.getUserId(), entity);
                else if (entity instanceof Training t) targetMap.put(t.getTrainingId(), entity);
            }
        }
    }
}