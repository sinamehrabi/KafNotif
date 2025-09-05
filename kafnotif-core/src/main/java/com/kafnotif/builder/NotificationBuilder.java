package com.kafnotif.builder;

import com.kafnotif.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder pattern for creating notifications easily
 */
public class NotificationBuilder {
    
    /**
     * Create an email notification builder
     * @param recipient email address
     * @return EmailBuilder instance
     */
    public static EmailBuilder email(String recipient) {
        return new EmailBuilder(recipient);
    }
    
    /**
     * Create an SMS notification builder
     * @param phoneNumber phone number
     * @return SmsBuilder instance
     */
    public static SmsBuilder sms(String phoneNumber) {
        return new SmsBuilder(phoneNumber);
    }
    
    /**
     * Create a push notification builder
     * @param deviceToken device token
     * @return PushBuilder instance
     */
    public static PushBuilder push(String deviceToken) {
        return new PushBuilder(deviceToken);
    }
    
    /**
     * Create a Slack notification builder
     * @param channel Slack channel
     * @return SlackBuilder instance
     */
    public static SlackBuilder slack(String channel) {
        return new SlackBuilder(channel);
    }
    
    /**
     * Create a Discord notification builder
     * @param webhookUrl Discord webhook URL
     * @return DiscordBuilder instance
     */
    public static DiscordBuilder discord(String webhookUrl) {
        return new DiscordBuilder(webhookUrl);
    }
    
    /**
     * Create a webhook notification builder
     * @param url webhook URL
     * @return WebhookBuilder instance
     */
    public static WebhookBuilder webhook(String url) {
        return new WebhookBuilder(url);
    }
    
    // Email Builder
    public static class EmailBuilder {
        private final EmailNotification notification;
        
        public EmailBuilder(String recipient) {
            this.notification = new EmailNotification();
            this.notification.setNotificationType(NotificationType.EMAIL);
            this.notification.setRecipient(recipient);
        }
        
        public EmailBuilder subject(String subject) {
            notification.setSubject(subject);
            return this;
        }
        
        public EmailBuilder body(String body) {
            notification.setBody(body);
            return this;
        }
        
        public EmailBuilder htmlBody(String htmlBody) {
            notification.setHtmlBody(htmlBody);
            return this;
        }
        
        public EmailBuilder cc(List<String> cc) {
            notification.setCc(cc);
            return this;
        }
        
        public EmailBuilder bcc(List<String> bcc) {
            notification.setBcc(bcc);
            return this;
        }
        
        public EmailBuilder from(String email, String name) {
            notification.setFromEmail(email);
            notification.setFromName(name);
            return this;
        }
        
        public EmailBuilder priority(NotificationPriority priority) {
            notification.setPriority(priority);
            return this;
        }
        
        public EmailBuilder scheduledAt(Instant scheduledAt) {
            notification.setScheduledAt(scheduledAt);
            return this;
        }
        
        public EmailNotification build() {
            return notification;
        }
    }
    
    // SMS Builder
    public static class SmsBuilder {
        private final SmsNotification notification;
        
        public SmsBuilder(String phoneNumber) {
            this.notification = new SmsNotification();
            this.notification.setNotificationType(NotificationType.SMS);
            this.notification.setRecipient(phoneNumber);
        }
        
        public SmsBuilder message(String message) {
            notification.setMessage(message);
            return this;
        }
        
        public SmsBuilder countryCode(String countryCode) {
            notification.setCountryCode(countryCode);
            return this;
        }
        
        public SmsBuilder provider(String provider) {
            notification.setProvider(provider);
            return this;
        }
        
        public SmsBuilder priority(NotificationPriority priority) {
            notification.setPriority(priority);
            return this;
        }
        
        public SmsNotification build() {
            return notification;
        }
    }
    
    // Push Builder
    public static class PushBuilder {
        private final PushNotification notification;
        
        public PushBuilder(String deviceToken) {
            this.notification = new PushNotification();
            this.notification.setNotificationType(NotificationType.PUSH);
            this.notification.setRecipient(deviceToken);
            this.notification.setDeviceToken(deviceToken);
        }
        
        public PushBuilder title(String title) {
            notification.setTitle(title);
            return this;
        }
        
        public PushBuilder body(String body) {
            notification.setBody(body);
            return this;
        }
        
        public PushBuilder icon(String icon) {
            notification.setIcon(icon);
            return this;
        }
        
        public PushBuilder badge(Integer badge) {
            notification.setBadge(badge);
            return this;
        }
        
        public PushBuilder sound(String sound) {
            notification.setSound(sound);
            return this;
        }
        
        public PushBuilder clickAction(String clickAction) {
            notification.setClickAction(clickAction);
            return this;
        }
        
        public PushBuilder data(Map<String, String> data) {
            notification.setData(data);
            return this;
        }
        
        public PushBuilder platform(PushNotification.PushPlatform platform) {
            notification.setPlatform(platform);
            return this;
        }
        
        public PushBuilder ttl(Integer ttl) {
            notification.setTtl(ttl);
            return this;
        }
        
        public PushNotification build() {
            return notification;
        }
    }
    
    // Slack Builder
    public static class SlackBuilder {
        private final SlackNotification notification;
        
        public SlackBuilder(String channel) {
            this.notification = new SlackNotification();
            this.notification.setNotificationType(NotificationType.SLACK);
            this.notification.setRecipient(channel);
            this.notification.setChannel(channel);
        }
        
        public SlackBuilder text(String text) {
            notification.setText(text);
            return this;
        }
        
        public SlackBuilder username(String username) {
            notification.setUsername(username);
            return this;
        }
        
        public SlackBuilder iconEmoji(String iconEmoji) {
            notification.setIconEmoji(iconEmoji);
            return this;
        }
        
        public SlackBuilder webhookUrl(String webhookUrl) {
            notification.setWebhookUrl(webhookUrl);
            return this;
        }
        
        public SlackBuilder threadTs(String threadTs) {
            notification.setThreadTs(threadTs);
            return this;
        }
        
        public SlackNotification build() {
            return notification;
        }
    }
    
    // Discord Builder
    public static class DiscordBuilder {
        private final DiscordNotification notification;
        
        public DiscordBuilder(String webhookUrl) {
            this.notification = new DiscordNotification();
            this.notification.setNotificationType(NotificationType.DISCORD);
            this.notification.setRecipient(webhookUrl);
            this.notification.setWebhookUrl(webhookUrl);
        }
        
        public DiscordBuilder content(String content) {
            notification.setContent(content);
            return this;
        }
        
        public DiscordBuilder username(String username) {
            notification.setUsername(username);
            return this;
        }
        
        public DiscordBuilder avatarUrl(String avatarUrl) {
            notification.setAvatarUrl(avatarUrl);
            return this;
        }
        
        public DiscordBuilder tts(boolean tts) {
            notification.setTts(tts);
            return this;
        }
        
        public DiscordNotification build() {
            return notification;
        }
    }
    
    // Webhook Builder
    public static class WebhookBuilder {
        private final WebhookNotification notification;
        
        public WebhookBuilder(String url) {
            this.notification = new WebhookNotification();
            this.notification.setNotificationType(NotificationType.WEBHOOK);
            this.notification.setRecipient(url);
            this.notification.setUrl(url);
            this.notification.setPayload(new HashMap<>());
        }
        
        public WebhookBuilder method(String method) {
            notification.setMethod(method);
            return this;
        }
        
        public WebhookBuilder payload(Map<String, Object> payload) {
            notification.setPayload(payload);
            return this;
        }
        
        public WebhookBuilder addPayload(String key, Object value) {
            notification.getWebhookPayload().put(key, value);
            return this;
        }
        
        public WebhookBuilder headers(Map<String, String> headers) {
            notification.setHeaders(headers);
            return this;
        }
        
        public WebhookBuilder contentType(String contentType) {
            notification.setContentType(contentType);
            return this;
        }
        
        public WebhookBuilder timeout(Integer timeout) {
            notification.setTimeout(timeout);
            return this;
        }
        
        public WebhookNotification build() {
            return notification;
        }
    }
}
