package com.epam.gymcrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class SecureConfig {
    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
