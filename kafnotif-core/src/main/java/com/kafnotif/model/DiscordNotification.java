package com.kafnotif.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Discord notification specific implementation
 */
public class DiscordNotification extends NotificationEvent {
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    
    @JsonProperty("tts")
    private boolean tts = false;
    
    @JsonProperty("embeds")
    private List<DiscordEmbed> embeds;
    
    @JsonProperty("webhookUrl")
    private String webhookUrl;
    
    @JsonProperty("channelId")
    private String channelId;

    public DiscordNotification() {
        super();
    }

    public DiscordNotification(String webhookUrl, String content) {
        super(NotificationType.DISCORD, webhookUrl);
        this.webhookUrl = webhookUrl;
        this.content = content;
    }

    public DiscordNotification(String channelId, String content, String webhookUrl) {
        this(webhookUrl, content);
        this.channelId = channelId;
    }

    @Override
    public boolean isValid() {
        return (content != null && !content.trim().isEmpty()) &&
               webhookUrl != null && !webhookUrl.trim().isEmpty() &&
               content.length() <= 2000; // Discord message limit
    }

    @Override
    public String getContent() {
        return content;
    }

    // Getters and Setters
    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isTts() {
        return tts;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }

    public List<DiscordEmbed> getEmbeds() {
        return embeds;
    }

    public void setEmbeds(List<DiscordEmbed> embeds) {
        this.embeds = embeds;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public static class DiscordEmbed {
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("color")
        private Integer color;
        
        @JsonProperty("fields")
        private List<DiscordField> fields;
        
        @JsonProperty("footer")
        private Map<String, String> footer;
        
        @JsonProperty("thumbnail")
        private Map<String, String> thumbnail;
        
        @JsonProperty("image")
        private Map<String, String> image;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Integer getColor() { return color; }
        public void setColor(Integer color) { this.color = color; }
        
        public List<DiscordField> getFields() { return fields; }
        public void setFields(List<DiscordField> fields) { this.fields = fields; }
        
        public Map<String, String> getFooter() { return footer; }
        public void setFooter(Map<String, String> footer) { this.footer = footer; }
        
        public Map<String, String> getThumbnail() { return thumbnail; }
        public void setThumbnail(Map<String, String> thumbnail) { this.thumbnail = thumbnail; }
        
        public Map<String, String> getImage() { return image; }
        public void setImage(Map<String, String> image) { this.image = image; }
    }

    public static class DiscordField {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("inline")
        private boolean inline = false;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public boolean isInline() { return inline; }
        public void setInline(boolean inline) { this.inline = inline; }
    }
}
