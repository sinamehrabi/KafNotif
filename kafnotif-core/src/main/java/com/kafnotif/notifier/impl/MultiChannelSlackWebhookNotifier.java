package com.kafnotif.notifier.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafnotif.util.JsonUtils;
import com.kafnotif.model.Event;
import com.kafnotif.model.SlackNotification;
import com.kafnotif.notifier.SlackNotifier;
import com.kafnotif.spring.AutomaticNotifierSetup.SlackConfig;
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
 * Enhanced Slack Webhook implementation with multi-channel support
 */
public class MultiChannelSlackWebhookNotifier implements SlackNotifier {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiChannelSlackWebhookNotifier.class);
    
    private final SlackConfig slackConfig;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor with SlackConfig for multi-channel support
     */
    public MultiChannelSlackWebhookNotifier(SlackConfig slackConfig) {
        this.slackConfig = slackConfig;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = JsonUtils.createObjectMapper();
    }
    
    /**
     * Constructor with custom HTTP client
     */
    public MultiChannelSlackWebhookNotifier(SlackConfig slackConfig, CloseableHttpClient httpClient) {
        this.slackConfig = slackConfig;
        this.httpClient = httpClient;
        this.objectMapper = JsonUtils.createObjectMapper();
    }
    
    @Override
    public boolean sendSlackMessage(SlackNotification notification) {
        try {
            // Resolve webhook URL for the specified channel
            String channel = notification.getChannel();
            if (channel == null || channel.trim().isEmpty()) {
                channel = slackConfig.getDefaultChannel();
                notification.setChannel(channel);
            }
            
            String webhookUrl = slackConfig.getWebhookUrlForChannel(channel);
            if (webhookUrl == null) {
                logger.error("No webhook URL configured for channel: {}. Available channels: {}", 
                    channel, slackConfig.getChannels().keySet());
                return false;
            }
            
            // Apply default username if not specified and available for this channel
            if (notification.getUsername() == null) {
                String defaultUsername = slackConfig.getDefaultUsernameForChannel(channel);
                if (defaultUsername != null) {
                    notification.setUsername(defaultUsername);
                }
            }
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", notification.getText());
            payload.put("channel", notification.getChannel());
            
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
            
            // Add thread support if present
            if (notification.getThreadTs() != null) {
                payload.put("thread_ts", notification.getThreadTs());
            }
            
            logger.debug("Sending Slack message to channel '{}' using webhook: {}", channel, webhookUrl);
            return sendSlackWebhook(webhookUrl, payload);
            
        } catch (Exception e) {
            logger.error("Failed to send Slack message to channel: {}", notification.getChannel(), e);
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
        SlackNotification notification = new SlackNotification();
        notification.setChannel(channel);
        notification.setText(text);
        notification.setThreadTs(threadTs);
        return sendSlackMessage(notification);
    }
    
    @Override
    public void send(Event event) {
        if (event instanceof SlackNotification) {
            sendSlackMessage((SlackNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be a SlackNotification");
        }
    }
    
    private boolean sendSlackWebhook(String webhookUrl, Map<String, Object> payload) {
        try {
            HttpPost request = new HttpPost(webhookUrl);
            String jsonBody = objectMapper.writeValueAsString(payload);
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                
                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Slack message sent successfully to channel '{}'. Status: {}", 
                        payload.get("channel"), statusCode);
                    return true;
                } else {
                    logger.error("Slack message failed for channel '{}'. Status: {}", 
                        payload.get("channel"), statusCode);
                    return false;
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to send Slack webhook to channel '{}': {}", 
                payload.get("channel"), e.getMessage());
            return false;
        }
    }
    
    /**
     * Get available channels
     */
    public Map<String, String> getAvailableChannels() {
        Map<String, String> channels = new HashMap<>();
        slackConfig.getChannels().forEach((name, config) -> 
            channels.put(name, config.getWebhookUrl()));
        return channels;
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
