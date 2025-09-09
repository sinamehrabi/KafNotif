package com.kafnotif.spring;

import com.kafnotif.config.ThreadingMode;
import com.kafnotif.consumer.AckMode;
import com.kafnotif.model.NotificationType;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Simple annotation to mark methods as KafNotif notification listeners.
 * This annotation just configures the consumer - no automatic processing.
 * Developers implement their own logic in the annotated method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface KafNotifListener {
    
    /**
     * The notification type to listen for
     */
    NotificationType value() default NotificationType.EMAIL;
    
    /**
     * Alternative way to specify multiple notification types
     */
    NotificationType[] types() default {};
    
    /**
     * Number of concurrent consumers for this listener
     */
    int concurrency() default 1;
    
    /**
     * Acknowledgment mode
     */
    AckMode ackMode() default AckMode.AUTO;
    
    /**
     * Threading mode (PLATFORM_THREADS or VIRTUAL_THREADS)
     */
    ThreadingMode threadingMode() default ThreadingMode.VIRTUAL_THREADS;
    
    /**
     * Maximum number of retries
     */
    int maxRetries() default 3;
    
    /**
     * Consumer group ID (optional)
     */
    String groupId() default "";
    
    /**
     * Bootstrap servers (optional, uses global config if empty)
     */
    String bootstrapServers() default "";
    
    /**
     * Topic prefix (optional, uses global config if empty)
     */
    String topicPrefix() default "";
    
    /**
     * Optional method name for after-send hook.
     * The method should have the signature:
     * public void methodName(NotificationEvent notification, boolean success, Exception error, AckControl ackControl)
     * 
     * @return the method name for after-send hook, empty string means no after-send hook
     */
    String afterSend() default "";
}
