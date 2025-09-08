package com.kafnotif.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafnotif.util.JsonUtils;
import com.kafnotif.config.ExecutorFactory;
import com.kafnotif.config.KafkaTopicManager;
import com.kafnotif.hooks.AckControl;
import com.kafnotif.hooks.NotificationHooks;
import com.kafnotif.model.NotificationEvent;
import com.kafnotif.model.NotificationType;
import com.kafnotif.notifier.NotificationProcessor;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Framework-agnostic notification consumer that works with any Java application
 */
public class NotificationConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    
    private final ConsumerConfig config;
    private final List<KafkaConsumer<String, String>> consumers;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final KafkaProducer<String, String> dlqProducer;
    private final KafkaTopicManager topicManager;
    
    // Locks per consumer for thread-safe acknowledgment
    private final Map<KafkaConsumer<String, String>, Object> consumerLocks = new ConcurrentHashMap<>();
    
    public NotificationConsumer(ConsumerConfig config) {
        this.config = config;
        this.objectMapper = JsonUtils.createObjectMapper();
        this.consumers = createConsumers();
        this.executorService = ExecutorFactory.create(config.getThreadingMode(), config.getMaxPoolSize());
        this.dlqProducer = config.isEnableDlq() ? createDlqProducer() : null;
        this.topicManager = new KafkaTopicManager(config.getBootstrapServers(), 
                                                config.getTopicPrefix(), 3, (short) 1);
        
        // Create topics if they don't exist
        createTopicsIfNeeded();
    }
    
    /**
     * Start consuming notifications
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("🚀 Starting notification consumer with {} concurrent consumers", config.getConcurrency());
            
            for (int i = 0; i < consumers.size(); i++) {
                final int consumerIndex = i;
                final KafkaConsumer<String, String> consumer = consumers.get(i);
                
                CompletableFuture.runAsync(() -> {
                    Thread.currentThread().setName("kafnotif-consumer-" + consumerIndex);
                    consumeLoop(consumer, consumerIndex);
                }, executorService);
            }
            
            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
            logger.info("✅ Notification consumer started successfully");
        }
    }
    
    /**
     * Stop consuming notifications
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("🛑 Stopping notification consumer...");
            
            consumers.forEach(KafkaConsumer::wakeup);
            
            try {
                Thread.sleep(1000); // Give consumers time to stop gracefully
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            consumers.forEach(KafkaConsumer::close);
            executorService.shutdown();
            
            if (dlqProducer != null) {
                dlqProducer.close();
            }
            
            topicManager.close();
            logger.info("✅ Notification consumer stopped");
        }
    }
    
    /**
     * Check if consumer is running
     */
    public boolean isRunning() {
        return running.get();
    }
    
    
    private List<KafkaConsumer<String, String>> createConsumers() {
        List<KafkaConsumer<String, String>> consumerList = new ArrayList<>();
        
        for (int i = 0; i < config.getConcurrency(); i++) {
            Properties props = new Properties();
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getOffsetReset());
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.isAutoCommit());
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
            props.put(org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
            
            if (!config.isAutoCommit()) {
                props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000);
            }
            
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            
            // Subscribe to notification topics
            List<String> topics = getTopicsToSubscribe();
            consumer.subscribe(topics);
            
            consumerList.add(consumer);
            consumerLocks.put(consumer, new Object()); // Initialize lock for this consumer
            logger.debug("Created consumer {} subscribing to topics: {}", i, topics);
        }
        
        return consumerList;
    }
    
    private List<String> getTopicsToSubscribe() {
        if (config.getNotificationTypes() != null && !config.getNotificationTypes().isEmpty()) {
            return config.getNotificationTypes().stream()
                    .map(type -> config.getTopicPrefix() + "." + type.getValue())
                    .collect(Collectors.toList());
        } else {
            // Subscribe to all notification types
            return Arrays.stream(NotificationType.values())
                    .map(type -> config.getTopicPrefix() + "." + type.getValue())
                    .collect(Collectors.toList());
        }
    }
    
    private void consumeLoop(KafkaConsumer<String, String> consumer, int consumerIndex) {
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(config.getPollTimeout());
                
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record, consumer, consumerIndex);
                }
                
                if (!config.isAutoCommit()) {
                    consumer.commitAsync((offsets, exception) -> {
                        if (exception != null) {
                            logger.error("Failed to commit offsets: {}", exception.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            if (running.get()) {
                logger.error("Error in consumer {}: {}", consumerIndex, e.getMessage(), e);
            }
        } finally {
            logger.debug("Consumer {} finished", consumerIndex);
        }
    }
    
    private void processRecord(ConsumerRecord<String, String> record, 
                             KafkaConsumer<String, String> consumer, int consumerIndex) {
        
        CompletableFuture.runAsync(() -> {
            AckControl ackControl = null;
            try {
                // Deserialize notification
                NotificationEvent notification = objectMapper.readValue(record.value(), NotificationEvent.class);
                
                logger.debug("🔄 Processing notification {} from topic {} [consumer-{}]", 
                           notification.getId(), record.topic(), consumerIndex);
                
                // Create ACK control for manual acknowledgment modes using original consumer
                if (config.getAckMode() != AckMode.AUTO) {
                    ackControl = new ThreadSafeAckControl(consumer, record, consumerLocks.get(consumer));
                }
                
                // Call beforeSend hook
                NotificationHooks hooks = config.getHooks();
                if (hooks != null && !hooks.beforeSend(notification, ackControl)) {
                    logger.info("⏭️ Notification {} skipped by beforeSend hook", notification.getId());
                    
                    // ACK if not already acknowledged and in AUTO mode
                    if (config.getAckMode() == AckMode.AUTO || 
                        (ackControl != null && !ackControl.isAcknowledged())) {
                        acknowledgeMessage(ackControl, record, consumer);
                    }
                    return;
                }
                
                // Process notification with retries
                boolean success = processWithRetries(notification);
                
                // Call afterSend hook
                if (hooks != null) {
                    hooks.afterSend(notification, success, null, ackControl);
                }
                
                if (success) {
                    logger.info("✅ Successfully processed notification: {}", notification.getId());
                    
                    // ACK successful messages if not already acknowledged
                    if (config.getAckMode() == AckMode.AUTO || 
                        (ackControl != null && !ackControl.isAcknowledged())) {
                        acknowledgeMessage(ackControl, record, consumer);
                    }
                } else {
                    logger.error("❌ Failed to process notification after all retries: {}", notification.getId());
                    
                    // Send to DLQ if enabled
                    if (config.isEnableDlq()) {
                        sendToDlq(notification, record.topic());
                    }
                    
                    // ACK failed messages if configured to do so (or if auto mode)
                    if (config.getAckMode() == AckMode.AUTO || 
                        (ackControl != null && !ackControl.isAcknowledged())) {
                        acknowledgeMessage(ackControl, record, consumer);
                    }
                }
                
            } catch (Exception e) {
                logger.error("💥 Error processing record from topic {}: {}", record.topic(), e.getMessage(), e);
                
                // ACK error messages to prevent infinite reprocessing
                if (config.getAckMode() == AckMode.AUTO || 
                    (ackControl != null && !ackControl.isAcknowledged())) {
                    acknowledgeMessage(ackControl, record, consumer);
                }
            }
        }, executorService);
    }
    
    private void acknowledgeMessage(AckControl ackControl, ConsumerRecord<String, String> record, 
                                  KafkaConsumer<String, String> consumer) {
        if (config.getAckMode() == AckMode.AUTO) {
            // Auto ACK is handled in the consumer loop
            return;
        }
        
        if (ackControl != null && !ackControl.isAcknowledged()) {
            try {
                if (config.getAckMode() == AckMode.MANUAL_IMMEDIATE) {
                    ackControl.acknowledge(); // Synchronous ACK on consumer thread
                } else if (config.getAckMode() == AckMode.MANUAL) {
                    // Use synchronous ACK to avoid thread safety issues with virtual threads
                    // The async nature is already provided by virtual threads processing
                    ackControl.acknowledge(); 
                }
            } catch (Exception e) {
                logger.error("Failed to acknowledge message: {}", e.getMessage(), e);
            }
        }
    }
    
    private boolean processWithRetries(NotificationEvent notification) {
        int attempt = 0;
        Throwable lastError = null;
        
        while (attempt <= config.getMaxRetries()) {
            try {
                boolean success = NotificationProcessor.processNotification(notification);
                if (success) {
                    return true;
                }
            } catch (Exception e) {
                lastError = e;
                logger.warn("Attempt {} failed for notification {}: {}", 
                          attempt + 1, notification.getId(), e.getMessage());
            }
            
            attempt++;
            
            if (attempt <= config.getMaxRetries()) {
                // Call retry hook
                NotificationHooks hooks = config.getHooks();
                if (hooks != null) {
                    hooks.onRetry(notification, attempt, config.getMaxRetries());
                }
                
                // Wait before retry
                try {
                    Thread.sleep(config.getRetryDelay().toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // Call permanent failure hook  
        // Note: AckControl is not available here since processWithRetries is called 
        // from processRecord where ACK control is managed
        NotificationHooks hooks = config.getHooks();
        if (hooks != null) {
            hooks.onPermanentFailure(notification, lastError, null);
        }
        
        return false;
    }
    
    private void sendToDlq(NotificationEvent notification, String originalTopic) {
        if (dlqProducer != null) {
            try {
                String dlqTopic = originalTopic + config.getDlqTopicSuffix();
                String payload = objectMapper.writeValueAsString(notification);
                
                ProducerRecord<String, String> dlqRecord = new ProducerRecord<>(
                    dlqTopic, notification.getId(), payload);
                
                dlqProducer.send(dlqRecord);
                logger.info("📤 Sent failed notification {} to DLQ: {}", notification.getId(), dlqTopic);
                
            } catch (Exception e) {
                logger.error("Failed to send notification {} to DLQ: {}", notification.getId(), e.getMessage(), e);
            }
        }
    }
    
    private KafkaProducer<String, String> createDlqProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        return new KafkaProducer<>(props);
    }
    
    private void createTopicsIfNeeded() {
        // Create topics for configured notification types
        List<String> topics = getTopicsToSubscribe();
        for (String topic : topics) {
            // Extract notification type from topic name
            String typeName = topic.substring(topic.lastIndexOf('.') + 1);
            try {
                NotificationType type = NotificationType.fromString(typeName);
                topicManager.createTopicIfNotExists(type);
                
                // Create DLQ topic if enabled
                if (config.isEnableDlq()) {
                    String dlqTopic = topic + config.getDlqTopicSuffix();
                    topicManager.createTopicIfNotExists(dlqTopic, 1, (short) 1);
                }
            } catch (Exception e) {
                logger.warn("Could not create topic for type: {}", typeName);
            }
        }
    }
    
    /**
     * Thread-safe wrapper for AckControl that synchronizes access to the KafkaConsumer
     */
    private static class ThreadSafeAckControl extends AckControl {
        private final Object consumerLock;
        
        public ThreadSafeAckControl(KafkaConsumer<String, String> consumer, ConsumerRecord<String, String> record, Object lock) {
            super(consumer, record);
            this.consumerLock = lock;
        }
        
        @Override
        public void acknowledge() {
            synchronized (consumerLock) {
                super.acknowledge();
            }
        }
        
        @Override
        public void acknowledgeAsync() {
            synchronized (consumerLock) {
                super.acknowledgeAsync();
            }
        }
    }
}
