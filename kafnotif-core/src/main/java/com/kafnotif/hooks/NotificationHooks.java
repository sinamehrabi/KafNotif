package com.kafnotif.hooks;

import com.kafnotif.model.NotificationEvent;

/**
 * Interface for notification lifecycle hooks
 */
public interface NotificationHooks {
    
    /**
     * Called before a notification is sent
     * @param notification the notification about to be sent
     * @param ackControl Control for manual acknowledgment (null if auto-ack is enabled)
     * @return true to continue with sending, false to skip
     */
    default boolean beforeSend(NotificationEvent notification, AckControl ackControl) {
        return beforeSend(notification);
    }
    
    /**
     * Called before a notification is sent (legacy method for backward compatibility)
     * @param notification the notification about to be sent
     * @return true to continue with sending, false to skip
     */
    default boolean beforeSend(NotificationEvent notification) {
        return true;
    }
    
    /**
     * Called after a notification is successfully sent
     * @param notification the notification that was sent
     * @param success whether the send was successful
     * @param error the error if send failed, null if successful
     * @param ackControl Control for manual acknowledgment (null if auto-ack is enabled)
     */
    default void afterSend(NotificationEvent notification, boolean success, Throwable error, AckControl ackControl) {
        afterSend(notification, success, error);
    }
    
    /**
     * Called after a notification is successfully sent (legacy method for backward compatibility)
     * @param notification the notification that was sent
     * @param success whether the send was successful
     * @param error the error if send failed, null if successful
     */
    default void afterSend(NotificationEvent notification, boolean success, Throwable error) {
        // Default implementation does nothing
    }
    
    /**
     * Called when a notification processing is retried
     * @param notification the notification being retried
     * @param retryAttempt the current retry attempt number
     * @param maxRetries the maximum number of retries
     */
    default void onRetry(NotificationEvent notification, int retryAttempt, int maxRetries) {
        // Default implementation does nothing
    }
    
    /**
     * Called when a notification fails permanently (max retries exceeded)
     * @param notification the notification that failed permanently
     * @param finalError the final error that caused permanent failure
     * @param ackControl Control for manual acknowledgment (null if auto-ack is enabled)
     */
    default void onPermanentFailure(NotificationEvent notification, Throwable finalError, AckControl ackControl) {
        onPermanentFailure(notification, finalError);
    }
    
    /**
     * Called when a notification fails permanently (max retries exceeded) (legacy method)
     * @param notification the notification that failed permanently
     * @param finalError the final error that caused permanent failure
     */
    default void onPermanentFailure(NotificationEvent notification, Throwable finalError) {
        // Default implementation does nothing
    }
}
