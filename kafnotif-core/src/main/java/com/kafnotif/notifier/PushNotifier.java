package com.kafnotif.notifier;

import com.kafnotif.model.PushNotification;
import com.kafnotif.model.Event;

/**
 * Push notification-specific notifier interface
 */
public interface PushNotifier extends Notifier {
    
    /**
     * Send a push notification
     * @param notification the push notification to send
     * @return true if sent successfully, false otherwise
     */
    boolean sendPushNotification(PushNotification notification);
    
    /**
     * Send a simple push notification
     * @param deviceToken device token for push notification
     * @param title notification title
     * @param body notification body
     * @return true if sent successfully, false otherwise
     */
    boolean sendSimplePush(String deviceToken, String title, String body);
    
    /**
     * Send push notification to multiple devices
     * @param deviceTokens array of device tokens
     * @param title notification title
     * @param body notification body
     * @return true if sent successfully to all devices, false otherwise
     */
    boolean sendPushToMultipleDevices(String[] deviceTokens, String title, String body);
    
    /**
     * Send push notification with custom data
     * @param notification the push notification with custom data
     * @return true if sent successfully, false otherwise
     */
    boolean sendPushWithData(PushNotification notification);
    
    @Override
    default void send(Event event) {
        if (event instanceof PushNotification) {
            sendPushNotification((PushNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be a PushNotification");
        }
    }
}
