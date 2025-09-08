package com.kafnotif.hooks;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * Thread-safe adapter that makes legacy AckControl delegate to the new Acknowledgment system
 * This allows existing hooks to continue working while using the thread-safe acknowledgment
 */
public class ThreadSafeAckControl extends AckControl {
    
    private final Acknowledgment acknowledgment;
    
    public ThreadSafeAckControl(KafkaConsumer<String, String> consumer, 
                                ConsumerRecord<String, String> record,
                                Acknowledgment acknowledgment) {
        super(consumer, record);
        this.acknowledgment = acknowledgment;
    }
    
    @Override
    public void acknowledge() {
        // Delegate to the thread-safe acknowledgment system
        acknowledgment.acknowledge();
    }
    
    @Override
    public void acknowledgeAsync() {
        // Delegate to the thread-safe acknowledgment system
        acknowledgment.acknowledge();
    }
    
    @Override
    public boolean isAcknowledged() {
        return acknowledgment.isAcknowledged();
    }
}
