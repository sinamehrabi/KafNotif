package com.example.demo;

import com.kafnotif.kafka.NotificationPublisher;
import com.kafnotif.model.DiscordNotification;
import com.kafnotif.model.EmailNotification;
import com.kafnotif.model.PushNotification;
import com.kafnotif.model.SlackNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationPublisher publisher;

    @PostMapping("/email")
    public String sendEmail(@RequestBody Map<String, String> request) {
        String to = request.getOrDefault("to", "test@example.com");
        String subject = request.getOrDefault("subject", "Test Email from KafNotif");
        String body = request.getOrDefault("body", "This is a test email sent at " + LocalDateTime.now());

        EmailNotification email = new EmailNotification(to, subject, body);

        publisher.publishNotification(email);
        return "📧 Email notification queued for: " + to;
    }

    @PostMapping("/push")
    public String sendPush(@RequestBody Map<String, String> request) {
        String deviceToken = request.getOrDefault("deviceToken", "test-device-token-123");
        String title = request.getOrDefault("title", "Test Push Notification");
        String body = request.getOrDefault("body", "This is a test push notification from KafNotif at " + LocalDateTime.now());
        String platform = request.getOrDefault("platform", "ANDROID");

        PushNotification push = new PushNotification(
                deviceToken, title, body, PushNotification.PushPlatform.valueOf(platform)
        );
        push.setIcon("notification_icon");
        push.setSound("default");

        publisher.publishNotification(push);
        return "🔔 Push notification queued for: " + deviceToken;
    }

    @PostMapping("/slack")
    public String sendSlack(@RequestBody Map<String, String> request) {
        String channel = request.getOrDefault("channel", "general");
        String text = request.getOrDefault("text", "Test Slack message from KafNotif at " + LocalDateTime.now());
        String username = request.getOrDefault("username", "KafNotif Bot");

        SlackNotification slack = new SlackNotification(channel, text);
        slack.setUsername(username);
        slack.setIconEmoji(":robot_face:");

        publisher.publishNotification(slack);
        return "💬 Slack notification sent to #" + channel;
    }

    @PostMapping("/discord")
    public String sendDiscord(@RequestBody Map<String, String> request) {
        String channel = request.getOrDefault("channel", "general");
        String content = request.getOrDefault("content", "Test Discord message from KafNotif at " + LocalDateTime.now());
        String username = request.getOrDefault("username", "KafNotif Bot");
        
        // For multi-channel Discord, we use the channelId constructor
        // The webhook URL will be resolved automatically by MultiChannelDiscordWebhookNotifier
        String webhookUrl = "https://discord.com/api/webhooks/PLACEHOLDER"; // Will be resolved by channel config

        DiscordNotification discord = new DiscordNotification(channel, content, webhookUrl);
        discord.setUsername(username);
        discord.setAvatarUrl("https://example.com/avatar.png");

        publisher.publishNotification(discord);
        return "🎮 Discord notification sent to #" + channel;
    }

    @GetMapping("/health")
    public String health() {
        return "🚀 KafNotif Example Application is running!";
    }
}
