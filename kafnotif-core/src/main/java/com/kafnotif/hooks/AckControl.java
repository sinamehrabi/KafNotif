package com.kafnotif.hooks;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controls manual acknowledgment of Kafka messages
 */
public class AckControl {
    
    private static final Logger logger = LoggerFactory.getLogger(AckControl.class);
    
    private final KafkaConsumer<String, String> consumer;
    private final ConsumerRecord<String, String> record;
    private final Map<TopicPartition, OffsetAndMetadata> offsetsToCommit;
    private volatile boolean acknowledged = false;
    
    public AckControl(KafkaConsumer<String, String> consumer, ConsumerRecord<String, String> record) {
        this.consumer = consumer;
        this.record = record;
        this.offsetsToCommit = new ConcurrentHashMap<>();
    }
    
    /**
     * Acknowledge this message (commit the offset)
     */
    public void acknowledge() {
        if (acknowledged) {
            logger.warn("Message from topic {} partition {} offset {} already acknowledged", 
                record.topic(), record.partition(), record.offset());
            return;
        }
        
        try {
            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
            OffsetAndMetadata offsetMetadata = new OffsetAndMetadata(record.offset() + 1);
            offsetsToCommit.put(topicPartition, offsetMetadata);
            
            consumer.commitSync(offsetsToCommit);
            acknowledged = true;
            
            logger.debug("✅ Acknowledged message from topic {} partition {} offset {}", 
                record.topic(), record.partition(), record.offset());
                
        } catch (Exception e) {
            logger.error("❌ Failed to acknowledge message from topic {} partition {} offset {}: {}", 
                record.topic(), record.partition(), record.offset(), e.getMessage(), e);
            throw new RuntimeException("Failed to acknowledge message", e);
        }
    }
    
    /**
     * Acknowledge this message asynchronously
     */
    public void acknowledgeAsync() {
        if (acknowledged) {
            logger.warn("Message from topic {} partition {} offset {} already acknowledged", 
                record.topic(), record.partition(), record.offset());
            return;
        }
        
        try {
            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
            OffsetAndMetadata offsetMetadata = new OffsetAndMetadata(record.offset() + 1);
            offsetsToCommit.put(topicPartition, offsetMetadata);
            
            consumer.commitAsync(offsetsToCommit, (offsets, exception) -> {
                if (exception != null) {
                    logger.error("❌ Failed to acknowledge message asynchronously from topic {} partition {} offset {}: {}", 
                        record.topic(), record.partition(), record.offset(), exception.getMessage(), exception);
                } else {
                    logger.debug("✅ Acknowledged message asynchronously from topic {} partition {} offset {}", 
                        record.topic(), record.partition(), record.offset());
                }
            });
            
            acknowledged = true;
            
        } catch (Exception e) {
            logger.error("❌ Failed to acknowledge message asynchronously from topic {} partition {} offset {}: {}", 
                record.topic(), record.partition(), record.offset(), e.getMessage(), e);
            throw new RuntimeException("Failed to acknowledge message asynchronously", e);
        }
    }
    
    /**
     * Check if this message has been acknowledged
     */
    public boolean isAcknowledged() {
        return acknowledged;
    }
    
    /**
     * Get the Kafka record
     */
    public ConsumerRecord<String, String> getRecord() {
        return record;
    }
    
    /**
     * Get message details for logging
     */
    public String getMessageInfo() {
        return String.format("topic=%s, partition=%d, offset=%d, key=%s", 
            record.topic(), record.partition(), record.offset(), record.key());
    }
}
