package com.kafnotif.hooks;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe acknowledgment implementation that queues acknowledgments
 * for processing on the main consumer thread (similar to Spring Kafka approach)
 */
public class KafNotifAcknowledgment implements Acknowledgment {
    
    private static final Logger logger = LoggerFactory.getLogger(KafNotifAcknowledgment.class);
    
    private final ConsumerRecord<String, String> record;
    private final ConcurrentLinkedQueue<AckRequest> ackQueue;
    private final AtomicBoolean acknowledged = new AtomicBoolean(false);
    
    public KafNotifAcknowledgment(ConsumerRecord<String, String> record, 
                                  ConcurrentLinkedQueue<AckRequest> ackQueue) {
        this.record = record;
        this.ackQueue = ackQueue;
    }
    
    @Override
    public void acknowledge() {
        if (acknowledged.compareAndSet(false, true)) {
            // Queue the acknowledgment for processing on the main consumer thread
            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
            AckRequest ackRequest = new AckRequest(topicPartition, record.offset());
            ackQueue.offer(ackRequest);
            
            logger.debug("✅ Queued acknowledgment for {}:{} offset {}", 
                        record.topic(), record.partition(), record.offset());
        }
    }
    
    @Override
    public boolean isAcknowledged() {
        return acknowledged.get();
    }
    
    /**
     * Request to acknowledge a specific offset
     */
    public static class AckRequest {
        private final TopicPartition topicPartition;
        private final long offset;
        
        public AckRequest(TopicPartition topicPartition, long offset) {
            this.topicPartition = topicPartition;
            this.offset = offset;
        }
        
        public TopicPartition getTopicPartition() {
            return topicPartition;
        }
        
        public long getOffset() {
            return offset;
        }
    }
}
