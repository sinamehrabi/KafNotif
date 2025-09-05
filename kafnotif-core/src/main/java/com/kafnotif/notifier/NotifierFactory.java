package com.kafnotif.notifier;

import com.kafnotif.model.NotificationType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and managing notification handlers
 */
public class NotifierFactory {
    
    private static final Map<NotificationType, Notifier> notifiers = new ConcurrentHashMap<>();
    
    /**
     * Register a notifier for a specific notification type
     * @param type the notification type
     * @param notifier the notifier implementation
     */
    public static void registerNotifier(NotificationType type, Notifier notifier) {
        notifiers.put(type, notifier);
    }
    
    /**
     * Get a notifier for a specific notification type
     * @param type the notification type
     * @return the notifier implementation or null if not registered
     */
    public static Notifier getNotifier(NotificationType type) {
        return notifiers.get(type);
    }
    
    /**
     * Check if a notifier is registered for a specific type
     * @param type the notification type
     * @return true if registered, false otherwise
     */
    public static boolean isNotifierRegistered(NotificationType type) {
        return notifiers.containsKey(type);
    }
    
    /**
     * Remove a notifier for a specific type
     * @param type the notification type
     */
    public static void unregisterNotifier(NotificationType type) {
        notifiers.remove(type);
    }
    
    /**
     * Clear all registered notifiers
     */
    public static void clearAll() {
        notifiers.clear();
    }
    
    /**
     * Get all registered notification types
     * @return array of registered notification types
     */
    public static NotificationType[] getRegisteredTypes() {
        return notifiers.keySet().toArray(new NotificationType[0]);
    }
}
