package com.kafnotif.consumer;

import com.kafnotif.config.ThreadingMode;
import com.kafnotif.hooks.NotificationHooks;
import com.kafnotif.model.NotificationType;

import java.time.Duration;
import java.util.Set;

/**
 * Configuration for notification consumers
 */
public class ConsumerConfig {
    
    private String bootstrapServers = "localhost:9092";
    private String groupId;
    private String topicPrefix = "notifications";
    private Set<NotificationType> notificationTypes;
    
    // Threading configuration
    private ThreadingMode threadingMode = ThreadingMode.PLATFORM_THREADS;
    private int concurrency = 3;
    private int maxPoolSize = 10;
    
    // Kafka consumer configuration
    private boolean autoCommit = false;
    private AckMode ackMode = AckMode.AUTO; // AUTO, MANUAL, MANUAL_IMMEDIATE
    private Duration pollTimeout = Duration.ofMillis(1000);
    private String offsetReset = "earliest";
    
    // Retry configuration
    private int maxRetries = 3;
    private Duration retryDelay = Duration.ofSeconds(5);
    private boolean enableRetries = true;
    
    // Hooks
    private NotificationHooks hooks;
    
    // Dead letter queue
    private boolean enableDlq = false;
    private String dlqTopicSuffix = ".dlq";
    
    public ConsumerConfig(String groupId) {
        this.groupId = groupId;
    }
    
    // Builder pattern methods
    public ConsumerConfig bootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
        return this;
    }
    
    public ConsumerConfig topicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
        return this;
    }
    
    public ConsumerConfig notificationTypes(Set<NotificationType> types) {
        this.notificationTypes = types;
        return this;
    }
    
    public ConsumerConfig threadingMode(ThreadingMode mode) {
        this.threadingMode = mode;
        return this;
    }
    
    public ConsumerConfig concurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;
    }
    
    public ConsumerConfig maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }
    
    public ConsumerConfig autoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }
    
    public ConsumerConfig ackMode(AckMode ackMode) {
        this.ackMode = ackMode;
        // Automatically set autoCommit based on ackMode
        this.autoCommit = (ackMode == AckMode.AUTO);
        return this;
    }
    
    public ConsumerConfig pollTimeout(Duration timeout) {
        this.pollTimeout = timeout;
        return this;
    }
    
    public ConsumerConfig offsetReset(String offsetReset) {
        this.offsetReset = offsetReset;
        return this;
    }
    
    public ConsumerConfig maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }
    
    public ConsumerConfig retryDelay(Duration delay) {
        this.retryDelay = delay;
        return this;
    }
    
    public ConsumerConfig enableRetries(boolean enable) {
        this.enableRetries = enable;
        return this;
    }
    
    public ConsumerConfig hooks(NotificationHooks hooks) {
        this.hooks = hooks;
        return this;
    }
    
    public ConsumerConfig enableDlq(boolean enable) {
        this.enableDlq = enable;
        return this;
    }
    
    public ConsumerConfig dlqTopicSuffix(String suffix) {
        this.dlqTopicSuffix = suffix;
        return this;
    }
    
    // Getters
    public String getBootstrapServers() { return bootstrapServers; }
    public String getGroupId() { return groupId; }
    public String getTopicPrefix() { return topicPrefix; }
    public Set<NotificationType> getNotificationTypes() { return notificationTypes; }
    public ThreadingMode getThreadingMode() { return threadingMode; }
    public int getConcurrency() { return concurrency; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public boolean isAutoCommit() { return autoCommit; }
    public AckMode getAckMode() { return ackMode; }
    public Duration getPollTimeout() { return pollTimeout; }
    public String getOffsetReset() { return offsetReset; }
    public int getMaxRetries() { return maxRetries; }
    public Duration getRetryDelay() { return retryDelay; }
    public boolean isEnableRetries() { return enableRetries; }
    public NotificationHooks getHooks() { return hooks; }
    public boolean isEnableDlq() { return enableDlq; }
    public String getDlqTopicSuffix() { return dlqTopicSuffix; }
}
