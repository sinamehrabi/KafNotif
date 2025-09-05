package com.kafnotif.config;

import com.kafnotif.model.NotificationType;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Manages Kafka topic creation and configuration for notifications
 */
public class KafkaTopicManager {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopicManager.class);
    
    private final AdminClient adminClient;
    private final String topicPrefix;
    private final int defaultPartitions;
    private final short defaultReplicationFactor;
    
    public KafkaTopicManager(String bootstrapServers, String topicPrefix, int defaultPartitions, short defaultReplicationFactor) {
        this.topicPrefix = topicPrefix;
        this.defaultPartitions = defaultPartitions;
        this.defaultReplicationFactor = defaultReplicationFactor;
        
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        adminProps.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        adminProps.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 30000);
        
        this.adminClient = AdminClient.create(adminProps);
    }
    
    /**
     * Create topic for a specific notification type if it doesn't exist
     */
    public void createTopicIfNotExists(NotificationType type) {
        String topicName = getTopicName(type);
        createTopicIfNotExists(topicName, defaultPartitions, defaultReplicationFactor);
    }
    
    /**
     * Create topic with custom configuration if it doesn't exist
     */
    public void createTopicIfNotExists(String topicName, int partitions, short replicationFactor) {
        try {
            // Check if topic exists
            Set<String> existingTopics = adminClient.listTopics().names().get();
            
            if (existingTopics.contains(topicName)) {
                logger.debug("Topic {} already exists", topicName);
                return;
            }
            
            // Create topic
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            
            // Set topic configurations for notifications
            Map<String, String> configs = new HashMap<>();
            configs.put("retention.ms", "604800000"); // 7 days retention
            configs.put("compression.type", "lz4");
            configs.put("min.insync.replicas", "1");
            newTopic.configs(configs);
            
            CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));
            result.all().get();
            
            logger.info("✅ Created Kafka topic: {} with {} partitions", topicName, partitions);
            
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                logger.debug("Topic {} already exists (race condition)", topicName);
            } else {
                logger.error("Failed to create topic: {}", topicName, e);
                throw new RuntimeException("Failed to create topic: " + topicName, e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while creating topic: " + topicName, e);
        }
    }
    
    /**
     * Create all notification topics
     */
    public void createAllNotificationTopics() {
        for (NotificationType type : NotificationType.values()) {
            createTopicIfNotExists(type);
        }
    }
    
    /**
     * Get topic name for notification type
     */
    public String getTopicName(NotificationType type) {
        return topicPrefix + "." + type.getValue();
    }
    
    /**
     * Get all notification topic names
     */
    public List<String> getAllNotificationTopics() {
        return Arrays.stream(NotificationType.values())
                .map(this::getTopicName)
                .collect(Collectors.toList());
    }
    
    /**
     * Check topic health and configuration
     */
    public Map<String, Object> getTopicInfo(String topicName) {
        try {
            DescribeTopicsResult result = adminClient.describeTopics(Collections.singleton(topicName));
            TopicDescription description = result.all().get().get(topicName);
            
            Map<String, Object> info = new HashMap<>();
            info.put("name", description.name());
            info.put("partitions", description.partitions().size());
            info.put("replicas", description.partitions().get(0).replicas().size());
            info.put("isInternal", description.isInternal());
            
            return info;
        } catch (Exception e) {
            logger.error("Failed to get topic info for: {}", topicName, e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * Close the admin client
     */
    public void close() {
        if (adminClient != null) {
            adminClient.close();
        }
    }
}
