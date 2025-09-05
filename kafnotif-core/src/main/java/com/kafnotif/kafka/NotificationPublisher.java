package com.kafnotif.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafnotif.config.KafkaTopicManager;
import com.kafnotif.model.NotificationEvent;
import com.kafnotif.model.NotificationType;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Enhanced publisher specifically for notification events
 */
public class NotificationPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationPublisher.class);
    
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseTopic;
    private final KafkaTopicManager topicManager;
    private final boolean autoCreateTopics;
    
    public NotificationPublisher(String bootstrapServers, String baseTopic) {
        this(bootstrapServers, baseTopic, true, 3, (short) 1);
    }
    
    public NotificationPublisher(String bootstrapServers, String baseTopic, boolean autoCreateTopics, 
                               int defaultPartitions, short defaultReplicationFactor) {
        this.baseTopic = baseTopic;
        this.autoCreateTopics = autoCreateTopics;
        
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        this.producer = new KafkaProducer<>(props);
        
        // Initialize topic manager if auto-creation is enabled
        if (autoCreateTopics) {
            this.topicManager = new KafkaTopicManager(bootstrapServers, baseTopic, 
                                                    defaultPartitions, defaultReplicationFactor);
            // Create all notification topics at startup
            this.topicManager.createAllNotificationTopics();
        } else {
            this.topicManager = null;
        }
    }
    
    /**
     * Publish a notification event to a type-specific topic
     * @param notification the notification to publish
     * @return Future for the send result
     */
    public Future<RecordMetadata> publishNotification(NotificationEvent notification) {
        if (!notification.isValid()) {
            throw new IllegalArgumentException("Invalid notification: " + notification);
        }
        
        try {
            String topic = getTopicForType(notification.getNotificationType());
            String payload = mapper.writeValueAsString(notification);
            
            ProducerRecord<String, String> record = new ProducerRecord<>(
                topic, 
                notification.getId(), 
                payload
            );
            
            // Add headers for better message routing and filtering
            record.headers().add("notificationType", notification.getNotificationType().getValue().getBytes());
            record.headers().add("priority", String.valueOf(notification.getPriority().getLevel()).getBytes());
            record.headers().add("retryCount", String.valueOf(notification.getRetryCount()).getBytes());
            
            Future<RecordMetadata> future = producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        logger.error("Failed to publish notification: {} to topic: {}. Error: {}", 
                                   notification.getId(), topic, exception.getMessage(), exception);
                    } else {
                        logger.info("📤 Published notification: {} to topic: {} at offset: {}", 
                                  notification.getId(), metadata.topic(), metadata.offset());
                    }
                }
            });
            
            return future;
            
        } catch (Exception e) {
            logger.error("Failed to serialize notification: {}", notification.getId(), e);
            throw new RuntimeException("Failed to publish notification", e);
        }
    }
    
    /**
     * Publish notification synchronously
     * @param notification the notification to publish
     * @return RecordMetadata for the published record
     */
    public RecordMetadata publishNotificationSync(NotificationEvent notification) {
        try {
            return publishNotification(notification).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish notification synchronously", e);
        }
    }
    
    /**
     * Publish to a specific topic (override type-based routing)
     * @param notification the notification to publish
     * @param topic the specific topic to publish to
     * @return Future for the send result
     */
    public Future<RecordMetadata> publishToTopic(NotificationEvent notification, String topic) {
        try {
            String payload = mapper.writeValueAsString(notification);
            
            ProducerRecord<String, String> record = new ProducerRecord<>(
                topic, 
                notification.getId(), 
                payload
            );
            
            // Add headers
            record.headers().add("notificationType", notification.getNotificationType().getValue().getBytes());
            record.headers().add("priority", String.valueOf(notification.getPriority().getLevel()).getBytes());
            
            return producer.send(record);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish notification to topic: " + topic, e);
        }
    }
    
    /**
     * Get the topic name for a specific notification type
     * @param type the notification type
     * @return the topic name
     */
    private String getTopicForType(NotificationType type) {
        return baseTopic + "." + type.getValue();
    }
    
    /**
     * Flush any pending messages
     */
    public void flush() {
        producer.flush();
    }
    
    /**
     * Close the publisher
     */
    public void close() {
        producer.close();
        if (topicManager != null) {
            topicManager.close();
        }
    }
}
