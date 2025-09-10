package com.kafnotif.notifier.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafnotif.model.DiscordNotification;
import com.kafnotif.model.Event;
import com.kafnotif.notifier.DiscordNotifier;
import com.kafnotif.spring.AutomaticNotifierSetup.DiscordConfig;
import com.kafnotif.util.JsonUtils;
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
import java.util.List;
import java.util.Map;

/**
 * Multi-channel Discord Webhook implementation of DiscordNotifier
 * Supports routing messages to different Discord channels based on configuration
 */
public class MultiChannelDiscordWebhookNotifier implements DiscordNotifier {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiChannelDiscordWebhookNotifier.class);
    
    private final DiscordConfig discordConfig;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public MultiChannelDiscordWebhookNotifier(DiscordConfig discordConfig) {
        this.discordConfig = discordConfig;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = JsonUtils.createObjectMapper();
    }
    
    @Override
    public boolean sendDiscordMessage(DiscordNotification notification) {
        String targetChannel = extractChannelFromNotification(notification);
        String webhookUrlToUse = null;
        String usernameToUse = notification.getUsername();
        
        if (targetChannel != null && !targetChannel.isEmpty()) {
            webhookUrlToUse = discordConfig.getWebhookUrlForChannel(targetChannel);
            if (usernameToUse == null) { // Only apply default if not explicitly set in notification
                usernameToUse = discordConfig.getDefaultUsernameForChannel(targetChannel);
            }
        }
        
        // Fallback to default channel if targetChannel not found or not specified
        if (webhookUrlToUse == null && discordConfig.getDefaultChannel() != null) {
            targetChannel = discordConfig.getDefaultChannel();
            webhookUrlToUse = discordConfig.getWebhookUrlForChannel(targetChannel);
            if (usernameToUse == null) {
                usernameToUse = discordConfig.getDefaultUsernameForChannel(targetChannel);
            }
        }
        
        // Final fallback to single webhookUrl for backward compatibility
        if (webhookUrlToUse == null) {
            webhookUrlToUse = discordConfig.getWebhookUrl();
        }
        
        if (webhookUrlToUse == null) {
            logger.error("❌ No Discord webhook URL configured for channel '{}' or default channel. Notification will not be sent.", targetChannel);
            return false;
        }
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", notification.getContent());
            
            if (usernameToUse != null) {
                payload.put("username", usernameToUse);
            }
            
            if (notification.getAvatarUrl() != null) {
                payload.put("avatar_url", notification.getAvatarUrl());
            }
            
            if (notification.getEmbeds() != null && !notification.getEmbeds().isEmpty()) {
                payload.put("embeds", notification.getEmbeds());
            }
            
            return sendDiscordWebhook(webhookUrlToUse, payload);
            
        } catch (Exception e) {
            logger.error("Failed to send Discord message to channel '{}'", targetChannel, e);
            return false;
        }
    }
    
    @Override
    public boolean sendSimpleMessage(String webhookUrl, String content) {
        // If specific webhook URL provided, use it directly
        if (webhookUrl != null && !webhookUrl.equals("placeholder")) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("content", content);
                return sendDiscordWebhook(webhookUrl, payload);
            } catch (Exception e) {
                logger.error("Failed to send simple Discord message to webhook: {}", webhookUrl, e);
                return false;
            }
        }
        
        // Otherwise use default channel
        String defaultChannel = discordConfig.getDefaultChannel();
        String webhookUrlToUse = discordConfig.getWebhookUrlForChannel(defaultChannel);
        
        if (webhookUrlToUse == null) {
            webhookUrlToUse = discordConfig.getWebhookUrl(); // Fallback to single webhook
        }
        
        if (webhookUrlToUse == null) {
            logger.error("❌ No Discord webhook URL configured for simple message.");
            return false;
        }
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", content);
            
            return sendDiscordWebhook(webhookUrlToUse, payload);
            
        } catch (Exception e) {
            logger.error("Failed to send simple Discord message", e);
            return false;
        }
    }
    
    @Override
    public boolean sendMessageWithEmbeds(DiscordNotification notification) {
        return sendDiscordMessage(notification); // Delegate to main method which handles embeds
    }
    
    @Override
    public boolean sendCustomMessage(String webhookUrl, String content, String username, String avatarUrl) {
        // If specific webhook URL provided, use it directly
        if (webhookUrl != null && !webhookUrl.equals("placeholder")) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("content", content);
                if (username != null) {
                    payload.put("username", username);
                }
                if (avatarUrl != null) {
                    payload.put("avatar_url", avatarUrl);
                }
                return sendDiscordWebhook(webhookUrl, payload);
            } catch (Exception e) {
                logger.error("Failed to send custom Discord message to webhook: {}", webhookUrl, e);
                return false;
            }
        }
        
        // Otherwise use default channel
        String defaultChannel = discordConfig.getDefaultChannel();
        String webhookUrlToUse = discordConfig.getWebhookUrlForChannel(defaultChannel);
        
        if (webhookUrlToUse == null) {
            webhookUrlToUse = discordConfig.getWebhookUrl(); // Fallback to single webhook
        }
        
        if (webhookUrlToUse == null) {
            logger.error("❌ No Discord webhook URL configured for custom message.");
            return false;
        }
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", content);
            if (username != null) {
                payload.put("username", username);
            }
            if (avatarUrl != null) {
                payload.put("avatar_url", avatarUrl);
            }
            
            return sendDiscordWebhook(webhookUrlToUse, payload);
            
        } catch (Exception e) {
            logger.error("Failed to send custom Discord message", e);
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
    
    /**
     * Extract channel information from Discord notification
     * Uses the channelId field from DiscordNotification if available
     */
    private String extractChannelFromNotification(DiscordNotification notification) {
        // First, try to use the channelId field if set
        if (notification.getChannelId() != null && !notification.getChannelId().isEmpty()) {
            return notification.getChannelId();
        }
        
        // Fallback: try to find a channel that matches the webhook URL
        String webhookUrl = notification.getWebhookUrl();
        for (Map.Entry<String, ?> entry : discordConfig.getChannels().entrySet()) {
            if (entry.getValue() instanceof com.kafnotif.spring.AutomaticNotifierSetup.DiscordChannelConfig) {
                com.kafnotif.spring.AutomaticNotifierSetup.DiscordChannelConfig channelConfig = 
                    (com.kafnotif.spring.AutomaticNotifierSetup.DiscordChannelConfig) entry.getValue();
                if (webhookUrl != null && webhookUrl.equals(channelConfig.getWebhookUrl())) {
                    return entry.getKey();
                }
            }
        }
        
        // If no specific channel found, use default
        return discordConfig.getDefaultChannel();
    }
    
    /**
     * Send webhook request to Discord
     */
    private boolean sendDiscordWebhook(String url, Map<String, Object> payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            HttpPost request = new HttpPost(url);
            request.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));
            request.setHeader("User-Agent", "KafNotif-Bot/1.0");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                
                if (statusCode >= 200 && statusCode < 300) {
                    logger.debug("✅ Discord webhook sent successfully to: {}", url);
                    return true;
                } else {
                    logger.error("❌ Discord webhook failed with status {}: {}", statusCode, url);
                    return false;
                }
            }
            
        } catch (IOException e) {
            logger.error("❌ Failed to send Discord webhook to: {}", url, e);
            return false;
        }
    }
}
