package com.kafnotif.notifier;

import com.kafnotif.model.WebhookNotification;
import com.kafnotif.model.Event;

import java.util.Map;

/**
 * Webhook-specific notifier interface
 */
public interface WebhookNotifier extends Notifier {
    
    /**
     * Send a webhook notification
     * @param notification the webhook notification to send
     * @return true if sent successfully, false otherwise
     */
    boolean sendWebhook(WebhookNotification notification);
    
    /**
     * Send a simple POST webhook
     * @param url webhook URL
     * @param payload JSON payload
     * @return true if sent successfully, false otherwise
     */
    boolean sendSimpleWebhook(String url, Map<String, Object> payload);
    
    /**
     * Send webhook with custom headers
     * @param url webhook URL
     * @param payload JSON payload
     * @param headers custom headers
     * @return true if sent successfully, false otherwise
     */
    boolean sendWebhookWithHeaders(String url, Map<String, Object> payload, Map<String, String> headers);
    
    /**
     * Send webhook with custom HTTP method
     * @param url webhook URL
     * @param method HTTP method (GET, POST, PUT, PATCH, DELETE)
     * @param payload JSON payload
     * @param headers custom headers
     * @return true if sent successfully, false otherwise
     */
    boolean sendCustomWebhook(String url, String method, Map<String, Object> payload, Map<String, String> headers);
    
    @Override
    default void send(Event event) {
        if (event instanceof WebhookNotification) {
            sendWebhook((WebhookNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be a WebhookNotification");
        }
    }
}
