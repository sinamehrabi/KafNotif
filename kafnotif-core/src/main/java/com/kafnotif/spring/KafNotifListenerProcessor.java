package com.kafnotif.spring;

import com.kafnotif.consumer.ConsumerConfig;
import com.kafnotif.consumer.NotificationConsumer;
import com.kafnotif.hooks.AckControl;
import com.kafnotif.hooks.NotificationHooks;
import com.kafnotif.model.NotificationEvent;
import com.kafnotif.model.NotificationType;
import com.kafnotif.notifier.NotifierFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple processor that discovers @KafNotifListener annotations and sets up consumers.
 * No automatic processing - just configures consumers and connects them to annotated methods.
 */
@Component
public class KafNotifListenerProcessor implements BeanPostProcessor, ApplicationContextAware {
    
    private static final Logger logger = LoggerFactory.getLogger(KafNotifListenerProcessor.class);
    
    private ApplicationContext applicationContext;
    private final KafNotifProperties properties;
    private final Map<String, NotificationConsumer> consumers = new ConcurrentHashMap<>();
    
    public KafNotifListenerProcessor(KafNotifProperties properties) {
        this.properties = properties;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Find all methods with @KafNotifListener annotation
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            KafNotifListener annotation = AnnotationUtils.findAnnotation(method, KafNotifListener.class);
            if (annotation != null) {
                processKafNotifListener(bean, method, annotation);
            }
        }, method -> true); // Add method filter parameter
        
        return bean;
    }
    
    private void processKafNotifListener(Object bean, Method method, KafNotifListener annotation) {
        Set<NotificationType> types = new HashSet<>();
        
        // Add main type
        if (annotation.value() != null) {
            types.add(annotation.value());
        }
        
        // Add additional types
        if (annotation.types().length > 0) {
            types.addAll(Arrays.asList(annotation.types()));
        }
        
        if (types.isEmpty()) {
            logger.warn("No notification types specified for method: {}.{}", 
                bean.getClass().getSimpleName(), method.getName());
            return;
        }
        
        // Create a consumer for this listener
        createConsumerForListener(bean, method, annotation, types);
    }
    
    private void createConsumerForListener(Object bean, Method method, KafNotifListener annotation, Set<NotificationType> types) {
        String consumerKey = bean.getClass().getSimpleName() + "." + method.getName();
        
        // Build consumer config
        ConsumerConfig config = new ConsumerConfig(getGroupId(annotation))
            .bootstrapServers(getBootstrapServers(annotation))
            .topicPrefix(getTopicPrefix(annotation))
            .notificationTypes(types)
            .threadingMode(annotation.threadingMode())
            .concurrency(annotation.concurrency())
            .ackMode(annotation.ackMode())
            .maxRetries(annotation.maxRetries())
            .enableDlq(properties.isEnableDlq());
        
        // Create hooks that will be called before/after automatic processing
        config.hooks(createListenerHooks(bean, method));
        
        // Create and start the consumer
        NotificationConsumer consumer = new NotificationConsumer(config);
        consumer.start();
        
        consumers.put(consumerKey, consumer);
        
        logger.info("✅ Started consumer for {}.{} - Types: {}, Concurrency: {}, ACK: {}, Threading: {}", 
            bean.getClass().getSimpleName(), method.getName(), 
            types, annotation.concurrency(), annotation.ackMode(), annotation.threadingMode());
    }
    
    private NotificationHooks createListenerHooks(Object bean, Method method) {
        return new NotificationHooks() {
            @Override
            public boolean beforeSend(NotificationEvent notification, AckControl ackControl) {
                // Call user method before automatic processing if method exists and is compatible
                try {
                    method.setAccessible(true);
                    Class<?>[] paramTypes = method.getParameterTypes();
                    
                    // If method takes notification parameter, call it as "before" hook
                    if (paramTypes.length == 1 && paramTypes[0].isAssignableFrom(notification.getClass())) {
                        logger.debug("Calling before hook: {}.{}", bean.getClass().getSimpleName(), method.getName());
                        method.invoke(bean, notification);
                        return true; // Continue with automatic processing
                    }
                } catch (Exception e) {
                    logger.error("Error in before hook {}.{}: {}", 
                        bean.getClass().getSimpleName(), method.getName(), e.getMessage(), e);
                }
                return true; // Continue with automatic processing even if hook fails
            }
            
            @Override
            public void afterSend(NotificationEvent notification, boolean success, Throwable error, AckControl ackControl) {
                if (success) {
                    logger.debug("✅ Automatic processing completed for {}: {} -> {}", 
                        notification.getNotificationType(), notification.getId(), notification.getRecipient());
                } else {
                    logger.error("❌ Automatic processing failed for {}: {} -> {} - {}", 
                        notification.getNotificationType(), notification.getId(), notification.getRecipient(), 
                        error != null ? error.getMessage() : "Unknown error");
                }
            }
        };
    }
    
    private String getBootstrapServers(KafNotifListener annotation) {
        return StringUtils.hasText(annotation.bootstrapServers()) ? 
            annotation.bootstrapServers() : properties.getBootstrapServers();
    }
    
    private String getTopicPrefix(KafNotifListener annotation) {
        return StringUtils.hasText(annotation.topicPrefix()) ? 
            annotation.topicPrefix() : properties.getTopicPrefix();
    }
    
    private String getGroupId(KafNotifListener annotation) {
        if (StringUtils.hasText(annotation.groupId())) {
            return annotation.groupId();
        }
        return properties.getGroupId();
    }
    
    @PreDestroy
    public void cleanup() {
        logger.info("🛑 Stopping all KafNotif consumers...");
        
        consumers.values().forEach(consumer -> {
            try {
                consumer.stop();
            } catch (Exception e) {
                logger.error("Error stopping consumer: {}", e.getMessage());
            }
        });
        
        consumers.clear();
        logger.info("✅ All KafNotif consumers stopped");
    }
}
