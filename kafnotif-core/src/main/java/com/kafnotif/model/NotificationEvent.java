package com.kafnotif.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced event model specifically for notifications
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "notificationType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = EmailNotification.class, name = "email"),
    @JsonSubTypes.Type(value = SmsNotification.class, name = "sms"),
    @JsonSubTypes.Type(value = PushNotification.class, name = "push"),
    @JsonSubTypes.Type(value = SlackNotification.class, name = "slack"),
    @JsonSubTypes.Type(value = DiscordNotification.class, name = "discord"),
    @JsonSubTypes.Type(value = WebhookNotification.class, name = "webhook")
})
public abstract class NotificationEvent extends Event {
    
    @JsonProperty("notificationType")
    private NotificationType notificationType;
    
    @JsonProperty("recipient")
    private String recipient;
    
    @JsonProperty("priority")
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    @JsonProperty("retryCount")
    private int retryCount = 0;
    
    @JsonProperty("maxRetries")
    private int maxRetries = 3;
    
    @JsonProperty("scheduledAt")
    private Instant scheduledAt;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public NotificationEvent() {
        super();
    }

    public NotificationEvent(NotificationType type, String recipient) {
        super(UUID.randomUUID().toString(), type.getValue(), null);
        this.notificationType = type;
        this.recipient = recipient;
        this.scheduledAt = Instant.now();
    }

    // Getters and Setters
    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Abstract method to be implemented by specific notification types
     * @return true if the notification data is valid
     */
    public abstract boolean isValid();

    /**
     * Abstract method to get the notification content
     * @return the notification content specific to the type
     */
    public abstract String getContent();

    @Override
    public String toString() {
        return "NotificationEvent{" +
                "id='" + getId() + '\'' +
                ", type=" + notificationType +
                ", recipient='" + recipient + '\'' +
                ", priority=" + priority +
                ", retryCount=" + retryCount +
                ", scheduledAt=" + scheduledAt +
                '}';
    }
}
