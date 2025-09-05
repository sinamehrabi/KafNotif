package com.kafnotif.examples.automatic;

import com.kafnotif.kafka.NotificationPublisher;
import com.kafnotif.model.*;
import com.kafnotif.model.PushNotification.PushPlatform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple REST controller to publish notifications and test the automatic processing
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationPublisher publisher;

    /**
     * Send a test email notification
     */
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody Map<String, Object> request) {
        try {
            String recipient = (String) request.getOrDefault("recipient", "test@example.com");
            String subject = (String) request.getOrDefault("subject", "Test Email from KafNotif");
            String body = (String) request.getOrDefault("body", "This is a test email sent via KafNotif at " + LocalDateTime.now());
            
            EmailNotification email = new EmailNotification(
                recipient, subject, body
            );
            email.setHtmlBody("<h1>" + subject + "</h1><p>" + body + "</p>");
            email.setPriority(NotificationPriority.NORMAL);
            
            publisher.publishNotification(email);
            
            System.out.println("📧 Email notification published: " + subject + " -> " + recipient);
            return ResponseEntity.ok("Email notification published successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to publish email notification: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to publish email: " + e.getMessage());
        }
    }

    /**
     * Send a test SMS notification
     */
    @PostMapping("/sms")
    public ResponseEntity<String> sendSms(@RequestBody Map<String, Object> request) {
        try {
            String recipient = (String) request.getOrDefault("recipient", "+1234567890");
            String message = (String) request.getOrDefault("message", "Test SMS from KafNotif at " + LocalDateTime.now());
            String countryCode = (String) request.getOrDefault("countryCode", "+1");
            
            SmsNotification sms = new SmsNotification(
                recipient, message
            );
            sms.setCountryCode(countryCode);
            
            publisher.publishNotification(sms);
            
            System.out.println("📱 SMS notification published: " + message + " -> " + recipient);
            return ResponseEntity.ok("SMS notification published successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to publish SMS notification: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to publish SMS: " + e.getMessage());
        }
    }

    /**
     * Send a test push notification
     */
    @PostMapping("/push")
    public ResponseEntity<String> sendPush(@RequestBody Map<String, Object> request) {
        try {
            String deviceToken = (String) request.getOrDefault("deviceToken", "test-device-token-123");
            String title = (String) request.getOrDefault("title", "Test Push Notification");
            String body = (String) request.getOrDefault("body", "This is a test push notification from KafNotif at " + LocalDateTime.now());
            String platform = (String) request.getOrDefault("platform", "ANDROID");
            
            PushNotification push = new PushNotification(
                deviceToken, title, body, PushPlatform.valueOf(platform)
            );
            push.setIcon("notification_icon");
            push.setSound("default");
            
            publisher.publishNotification(push);
            
            System.out.println("🔔 Push notification published: " + title + " -> " + deviceToken);
            return ResponseEntity.ok("Push notification published successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to publish push notification: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to publish push: " + e.getMessage());
        }
    }

    /**
     * Send a test Slack notification
     */
    @PostMapping("/slack")
    public ResponseEntity<String> sendSlack(@RequestBody Map<String, Object> request) {
        try {
            String channel = (String) request.getOrDefault("channel", "#general");
            String text = (String) request.getOrDefault("text", "Test Slack message from KafNotif at " + LocalDateTime.now());
            String username = (String) request.getOrDefault("username", "KafNotif Bot");
            
            SlackNotification slack = new SlackNotification(
                channel, text
            );
            slack.setUsername(username);
            slack.setIconEmoji(":robot_face:");
            
            publisher.publishNotification(slack);
            
            System.out.println("💬 Slack notification published: " + text + " -> " + channel);
            return ResponseEntity.ok("Slack notification published successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to publish Slack notification: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to publish Slack: " + e.getMessage());
        }
    }

    /**
     * Send a test Discord notification
     */
    @PostMapping("/discord")
    public ResponseEntity<String> sendDiscord(@RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.getOrDefault("content", "Test Discord message from KafNotif at " + LocalDateTime.now());
            String username = (String) request.getOrDefault("username", "KafNotif Bot");
            
            DiscordNotification discord = new DiscordNotification(
                "https://example.com/webhook", content
            );
            discord.setUsername(username);
            discord.setAvatarUrl("https://example.com/avatar.png");
            
            publisher.publishNotification(discord);
            
            System.out.println("🎮 Discord notification published: " + content);
            return ResponseEntity.ok("Discord notification published successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to publish Discord notification: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to publish Discord: " + e.getMessage());
        }
    }

    /**
     * Send a test webhook notification
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> sendWebhook(@RequestBody Map<String, Object> request) {
        try {
            String url = (String) request.getOrDefault("url", "https://httpbin.org/post");
            String method = (String) request.getOrDefault("method", "POST");
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) request.getOrDefault("payload", 
                Map.of("message", "Test webhook from KafNotif at " + LocalDateTime.now()));
            
            WebhookNotification webhook = new WebhookNotification(
                url, method, payload
            );
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("User-Agent", "KafNotif/1.0");
            webhook.setHeaders(headers);
            
            publisher.publishNotification(webhook);
            
            System.out.println("🌐 Webhook notification published: " + method + " " + url);
            return ResponseEntity.ok("Webhook notification published successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to publish webhook notification: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to publish webhook: " + e.getMessage());
        }
    }

    /**
     * Send multiple notifications at once
     */
    @PostMapping("/batch")
    public ResponseEntity<String> sendBatch() {
        try {
            // Send various notifications
            publisher.publishNotification(new EmailNotification(
                "batch@example.com", 
                "Batch Email Test", 
                "This is a batch email test"
            ));
            
            publisher.publishNotification(new SmsNotification(
                "+1987654321", 
                "Batch SMS test from KafNotif"
            ));
            
            publisher.publishNotification(new PushNotification(
                "batch-device-token", 
                "Batch Push Test", 
                "This is a batch push test", 
                PushPlatform.IOS
            ));
            
            System.out.println("🔀 Batch notifications published successfully");
            return ResponseEntity.ok("Batch notifications published successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to publish batch notifications: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to publish batch: " + e.getMessage());
        }
    }
}
