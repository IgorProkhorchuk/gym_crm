package com.epam.gymcrm.client.workload;

/**
 * Trainer workload delivery result.
 *
 * @param successful whether delivery succeeded
 * @param errorMessage delivery failure message
 */
public record TrainerWorkloadNotificationResult(boolean successful, String errorMessage) {}
