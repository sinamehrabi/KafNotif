package com.kafnotif.consumer;

/**
 * Acknowledgment modes for Kafka message processing
 */
public enum AckMode {
    
    /**
     * Automatic acknowledgment - messages are acknowledged automatically after processing
     */
    AUTO,
    
    /**
     * Manual acknowledgment - messages must be manually acknowledged in hooks
     * Acknowledgment happens asynchronously in batch
     */
    MANUAL,
    
    /**
     * Manual immediate acknowledgment - messages must be manually acknowledged in hooks
     * Acknowledgment happens immediately (synchronously)
     */
    MANUAL_IMMEDIATE
}
