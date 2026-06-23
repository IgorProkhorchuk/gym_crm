package com.epam.gymcrm.workload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration shared with Gym CRM system for internal service calls.
 *
 * @param issuer expected JWT issuer
 * @param secret shared HMAC secret
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String issuer, String secret) {}
