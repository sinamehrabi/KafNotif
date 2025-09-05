package com.kafnotif.notifier.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafnotif.model.DiscordNotification;
import com.kafnotif.model.Event;
import com.kafnotif.notifier.DiscordNotifier;
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
 * Discord Webhook implementation of DiscordNotifier
 */
public class DiscordWebhookNotifier implements DiscordNotifier {
    
    private static final Logger logger = LoggerFactory.getLogger(DiscordWebhookNotifier.class);
    
    private final String webhookUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor with Discord webhook URL
     */
    public DiscordWebhookNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Constructor with custom HTTP client
     */
    public DiscordWebhookNotifier(String webhookUrl, CloseableHttpClient httpClient) {
        this.webhookUrl = webhookUrl;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public boolean sendDiscordMessage(DiscordNotification notification) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", notification.getContent());
            
            if (notification.getUsername() != null) {
                payload.put("username", notification.getUsername());
            }
            
            if (notification.getAvatarUrl() != null) {
                payload.put("avatar_url", notification.getAvatarUrl());
            }
            
            if (notification.isTts()) {
                payload.put("tts", true);
            }
            
            // Add embeds if present
            if (notification.getEmbeds() != null && !notification.getEmbeds().isEmpty()) {
                payload.put("embeds", notification.getEmbeds());
            }
            
            // Add webhook URL if specified
            if (notification.getWebhookUrl() != null) {
                payload.put("webhookUrl", notification.getWebhookUrl());
            }
            
            return sendDiscordWebhook(payload);
            
        } catch (Exception e) {
            logger.error("Failed to send Discord message", e);
            return false;
        }
    }
    
    @Override
    public boolean sendSimpleMessage(String webhookUrl, String content) {
        DiscordNotification notification = new DiscordNotification();
        notification.setContent(content);
        notification.setWebhookUrl(webhookUrl);
        return sendDiscordMessage(notification);
    }
    
    @Override
    public boolean sendMessageWithEmbeds(DiscordNotification notification) {
        return sendDiscordMessage(notification);
    }
    
    @Override
    public boolean sendCustomMessage(String webhookUrl, String content, String username, String avatarUrl) {
        DiscordNotification notification = new DiscordNotification();
        notification.setContent(content);
        notification.setWebhookUrl(webhookUrl);
        notification.setUsername(username);
        notification.setAvatarUrl(avatarUrl);
        return sendDiscordMessage(notification);
    }
    
    @Override
    public void send(Event event) {
        if (event instanceof DiscordNotification) {
            sendDiscordMessage((DiscordNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be a DiscordNotification");
        }
    }
    
    private boolean sendDiscordWebhook(Map<String, Object> payload) {
        String url = webhookUrl; // Use default webhook URL
        
        // If payload contains webhook URL, use that instead
        if (payload.containsKey("webhookUrl")) {
            url = (String) payload.remove("webhookUrl");
        }
        
        try {
            HttpPost request = new HttpPost(url);
            String jsonBody = objectMapper.writeValueAsString(payload);
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                
                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Discord message sent successfully. Status: {}", statusCode);
                    return true;
                } else {
                    logger.error("Discord message failed. Status: {}", statusCode);
                    return false;
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to send Discord webhook", e);
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
