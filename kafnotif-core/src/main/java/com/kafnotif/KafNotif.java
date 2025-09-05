package com.kafnotif;

import com.kafnotif.consumer.ConsumerConfig;
import com.kafnotif.consumer.NotificationConsumer;
import com.kafnotif.kafka.NotificationPublisher;
import com.kafnotif.model.NotificationEvent;
import com.kafnotif.model.NotificationType;

import java.util.Set;

/**
 * Main entry point for KafNotif library
 * Provides a simple API for both publishing and consuming notifications
 */
public class KafNotif {
    
    /**
     * Create a notification publisher with auto-topic creation
     */
    public static NotificationPublisher createPublisher(String bootstrapServers) {
        return new NotificationPublisher(bootstrapServers, "notifications");
    }
    
    /**
     * Create a notification publisher with custom configuration
     */
    public static NotificationPublisher createPublisher(String bootstrapServers, String topicPrefix) {
        return new NotificationPublisher(bootstrapServers, topicPrefix);
    }
    
    /**
     * Create a notification publisher with full configuration
     */
    public static NotificationPublisher createPublisher(String bootstrapServers, String topicPrefix, 
                                                      boolean autoCreateTopics, int partitions, short replicationFactor) {
        return new NotificationPublisher(bootstrapServers, topicPrefix, autoCreateTopics, partitions, replicationFactor);
    }
    
    /**
     * Create a notification consumer with basic configuration
     */
    public static NotificationConsumer createConsumer(String bootstrapServers, String groupId) {
        ConsumerConfig config = new ConsumerConfig(groupId)
                .bootstrapServers(bootstrapServers);
        return new NotificationConsumer(config);
    }
    
    /**
     * Create a notification consumer for specific notification types
     */
    public static NotificationConsumer createConsumer(String bootstrapServers, String groupId, 
                                                    Set<NotificationType> notificationTypes) {
        ConsumerConfig config = new ConsumerConfig(groupId)
                .bootstrapServers(bootstrapServers)
                .notificationTypes(notificationTypes);
        return new NotificationConsumer(config);
    }
    
    /**
     * Create a consumer configuration builder
     */
    public static ConsumerConfig configureConsumer(String groupId) {
        return new ConsumerConfig(groupId);
    }
}
