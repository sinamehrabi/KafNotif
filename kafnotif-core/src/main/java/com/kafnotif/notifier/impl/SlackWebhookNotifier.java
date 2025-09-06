package com.kafnotif.notifier.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafnotif.util.JsonUtils;
import com.kafnotif.model.Event;
import com.kafnotif.model.SlackNotification;
import com.kafnotif.notifier.SlackNotifier;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Slack Webhook implementation of SlackNotifier
 */
public class SlackWebhookNotifier implements SlackNotifier {
    
    private static final Logger logger = LoggerFactory.getLogger(SlackWebhookNotifier.class);
    
    private final String webhookUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor with Slack webhook URL
     */
    public SlackWebhookNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = JsonUtils.createObjectMapper();
    }
    
    /**
     * Constructor with custom HTTP client
     */
    public SlackWebhookNotifier(String webhookUrl, CloseableHttpClient httpClient) {
        this.webhookUrl = webhookUrl;
        this.httpClient = httpClient;
        this.objectMapper = JsonUtils.createObjectMapper();
    }
    
    @Override
    public boolean sendSlackMessage(SlackNotification notification) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", notification.getText());
            
            if (notification.getChannel() != null) {
                payload.put("channel", notification.getChannel());
            }
            
            if (notification.getUsername() != null) {
                payload.put("username", notification.getUsername());
            }
            
            if (notification.getIconEmoji() != null) {
                payload.put("icon_emoji", notification.getIconEmoji());
            }
            
            if (notification.getIconUrl() != null) {
                payload.put("icon_url", notification.getIconUrl());
            }
            
            // Add attachments if present
            if (notification.getAttachments() != null && !notification.getAttachments().isEmpty()) {
                payload.put("attachments", notification.getAttachments());
            }
            
            // Add blocks if present
            if (notification.getBlocks() != null && !notification.getBlocks().isEmpty()) {
                payload.put("blocks", notification.getBlocks());
            }
            
            return sendSlackWebhook(payload);
            
        } catch (Exception e) {
            logger.error("Failed to send Slack message", e);
            return false;
        }
    }
    
    @Override
    public boolean sendSimpleMessage(String channel, String text) {
        SlackNotification notification = new SlackNotification();
        notification.setChannel(channel);
        notification.setText(text);
        return sendSlackMessage(notification);
    }
    
    @Override
    public boolean sendMessageWithAttachments(SlackNotification notification) {
        return sendSlackMessage(notification);
    }
    
    @Override
    public boolean sendThreadMessage(String channel, String text, String threadTs) {
        // For webhook implementation, we can't directly support thread replies
        // This would require the Slack Web API, not webhooks
        // For now, just send as regular message
        return sendSimpleMessage(channel, text + " (thread reply)");
    }
    
    @Override
    public void send(Event event) {
        if (event instanceof SlackNotification) {
            sendSlackMessage((SlackNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be a SlackNotification");
        }
    }
    
    private boolean sendSlackWebhook(Map<String, Object> payload) {
        try {
            HttpPost request = new HttpPost(webhookUrl);
            String jsonBody = objectMapper.writeValueAsString(payload);
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                
                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Slack message sent successfully. Status: {}", statusCode);
                    return true;
                } else {
                    logger.error("Slack message failed. Status: {}", statusCode);
                    return false;
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to send Slack webhook", e);
            return false;
        }
    }
    
    /**
     * Close the HTTP client
     */
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
