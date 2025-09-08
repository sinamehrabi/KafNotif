package com.kafnotif.hooks;

/**
 * Thread-safe acknowledgment interface similar to Spring Kafka's Acknowledgment
 * Allows virtual threads to safely acknowledge messages without direct KafkaConsumer access
 */
public interface Acknowledgment {
    
    /**
     * Acknowledge the current record.
     * This method is thread-safe and can be called from virtual threads.
     */
    void acknowledge();
    
    /**
     * Check if this acknowledgment has already been processed
     */
    boolean isAcknowledged();
}
