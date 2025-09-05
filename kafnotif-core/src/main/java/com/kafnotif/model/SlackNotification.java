package com.kafnotif.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Slack notification specific implementation
 */
public class SlackNotification extends NotificationEvent {
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("channel")
    private String channel;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("iconEmoji")
    private String iconEmoji;
    
    @JsonProperty("iconUrl")
    private String iconUrl;
    
    @JsonProperty("attachments")
    private List<SlackAttachment> attachments;
    
    @JsonProperty("blocks")
    private List<Map<String, Object>> blocks;
    
    @JsonProperty("webhookUrl")
    private String webhookUrl;
    
    @JsonProperty("threadTs")
    private String threadTs; // For threaded messages

    public SlackNotification() {
        super();
    }

    public SlackNotification(String channel, String text) {
        super(NotificationType.SLACK, channel);
        this.channel = channel;
        this.text = text;
    }

    public SlackNotification(String webhookUrl, String channel, String text) {
        this(channel, text);
        this.webhookUrl = webhookUrl;
    }

    @Override
    public boolean isValid() {
        return (channel != null && !channel.trim().isEmpty()) &&
               (text != null && !text.trim().isEmpty()) &&
               (webhookUrl != null || getRecipient() != null);
    }

    @Override
    public String getContent() {
        return text;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<SlackAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<SlackAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<Map<String, Object>> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Map<String, Object>> blocks) {
        this.blocks = blocks;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getThreadTs() {
        return threadTs;
    }

    public void setThreadTs(String threadTs) {
        this.threadTs = threadTs;
    }

    public static class SlackAttachment {
        @JsonProperty("color")
        private String color;
        
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("fields")
        private List<SlackField> fields;

        // Getters and Setters
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public List<SlackField> getFields() { return fields; }
        public void setFields(List<SlackField> fields) { this.fields = fields; }
    }

    public static class SlackField {
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("short")
        private boolean isShort;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public boolean isShort() { return isShort; }
        public void setShort(boolean isShort) { this.isShort = isShort; }
    }
}
