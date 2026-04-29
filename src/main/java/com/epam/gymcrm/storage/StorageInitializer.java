package com.epam.gymcrm.storage;

import com.epam.gymcrm.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class StorageInitializer implements BeanPostProcessor {

    @Value("classpath:${storage.file.path}")
    private Resource storageFile;

    private final ObjectMapper objectMapper;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof InMemoryStorage storage) {
            if (!storageFile.exists()) {
                log.warn("Storage file {} does not exist. Skipping pre-population.", storageFile.getFilename());
                return bean;
            }

            try (InputStream inputStream = storageFile.getInputStream()) {
                JsonNode root = objectMapper.readTree(inputStream);

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
                T entity = objectMapper.treeToValue(n, type);
                Long id = type == Training.class
                        ? ((Training) entity).getTrainingId()
                        : ((User) entity).getUserId();
                targetMap.put(id, entity);
            }
        }
    }
}
