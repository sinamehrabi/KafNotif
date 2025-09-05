package com.kafnotif.spring;

import com.kafnotif.config.ThreadingMode;
import com.kafnotif.consumer.AckMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global KafNotif configuration properties for Spring Boot
 */
@ConfigurationProperties(prefix = "kafnotif")
public class KafNotifProperties {
    
    /**
     * Kafka bootstrap servers
     */
    private String bootstrapServers = "localhost:9092";
    
    /**
     * Consumer group ID prefix
     */
    private String groupId = "kafnotif-consumer";
    
    /**
     * Topic prefix for notifications
     */
    private String topicPrefix = "notifications";
    
    /**
     * Default threading mode
     */
    private ThreadingMode threadingMode = ThreadingMode.VIRTUAL_THREADS;
    
    /**
     * Default concurrency
     */
    private int concurrency = 1;
    
    /**
     * Default ACK mode
     */
    private AckMode ackMode = AckMode.AUTO;
    
    /**
     * Default max retries
     */
    private int maxRetries = 3;
    
    /**
     * Enable DLQ by default
     */
    private boolean enableDlq = true;

    // Getters and setters
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public ThreadingMode getThreadingMode() {
        return threadingMode;
    }

    public void setThreadingMode(ThreadingMode threadingMode) {
        this.threadingMode = threadingMode;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public AckMode getAckMode() {
        return ackMode;
    }

    public void setAckMode(AckMode ackMode) {
        this.ackMode = ackMode;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean isEnableDlq() {
        return enableDlq;
    }

    public void setEnableDlq(boolean enableDlq) {
        this.enableDlq = enableDlq;
    }
}
