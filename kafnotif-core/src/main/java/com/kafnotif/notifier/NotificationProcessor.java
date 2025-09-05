package com.kafnotif.notifier;

import com.kafnotif.hooks.NotificationHooks;
import com.kafnotif.model.NotificationEvent;
import com.kafnotif.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central processor for handling notification events
 */
public class NotificationProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessor.class);
    
    /**
     * Process a notification event using the appropriate notifier
     * @param notification the notification event to process
     * @return true if processed successfully, false otherwise
     */
    public static boolean processNotification(NotificationEvent notification) {
        if (notification == null) {
            logger.error("Notification event is null");
            return false;
        }
        
        if (!notification.isValid()) {
            logger.error("Invalid notification event: {}", notification);
            return false;
        }
        
        NotificationType type = notification.getNotificationType();
        Notifier notifier = NotifierFactory.getNotifier(type);
        
        if (notifier == null) {
            logger.error("No notifier registered for type: {}", type);
            return false;
        }
        
        try {
            notifier.send(notification);
            logger.info("Successfully processed notification: {} for recipient: {}", 
                       type, notification.getRecipient());
            return true;
        } catch (Exception e) {
            logger.error("Failed to process notification: {} for recipient: {}. Error: {}", 
                        type, notification.getRecipient(), e.getMessage(), e);
            
            // Handle retry logic
            if (notification.getRetryCount() < notification.getMaxRetries()) {
                notification.setRetryCount(notification.getRetryCount() + 1);
                logger.info("Scheduling retry {} for notification: {}", 
                           notification.getRetryCount(), notification.getId());
                // Note: Actual retry scheduling would be handled by the consumer service
            }
            
            return false;
        }
    }
    
    /**
     * Process notification with retry handling
     * @param notification the notification event to process
     * @param maxRetries maximum number of retries
     * @return true if processed successfully, false otherwise
     */
    public static boolean processNotificationWithRetry(NotificationEvent notification, int maxRetries) {
        notification.setMaxRetries(maxRetries);
        return processNotification(notification);
    }
    
    /**
     * Process notification with custom hooks
     * @param notification the notification event to process
     * @param hooks custom notification hooks
     * @return true if processed successfully, false otherwise
     */
    public static boolean processNotificationWithHooks(NotificationEvent notification, NotificationHooks hooks) {
        if (hooks != null && !hooks.beforeSend(notification)) {
            logger.info("Notification {} skipped by beforeSend hook", notification.getId());
            return true; // Consider as successful since it was intentionally skipped
        }
        
        boolean success = false;
        Throwable error = null;
        
        try {
            success = processNotification(notification);
        } catch (Exception e) {
            error = e;
            logger.error("Error processing notification with hooks: {}", notification.getId(), e);
        }
        
        if (hooks != null) {
            hooks.afterSend(notification, success, error);
        }
        
        return success;
    }
}
