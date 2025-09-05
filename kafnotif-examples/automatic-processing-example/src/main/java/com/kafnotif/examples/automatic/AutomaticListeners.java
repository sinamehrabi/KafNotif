package com.kafnotif.examples.automatic;

import com.kafnotif.config.ThreadingMode;
import com.kafnotif.consumer.AckMode;
import com.kafnotif.model.*;
import com.kafnotif.spring.KafNotifListener;
import org.springframework.stereotype.Component;

/**
 * 🤖 FULLY AUTOMATIC Notification Processing!
 * 
 * ✨ What happens here:
 * 1. You publish notifications via REST API
 * 2. KafNotif AUTOMATICALLY sends them using:
 *    📧 JavaMail for emails
 *    📱 Twilio for SMS  
 *    🔔 Firebase for push notifications
 *    💬 Slack webhooks
 *    🎮 Discord webhooks
 *    🌐 HTTP calls for webhooks
 * 
 * 🎯 The methods below are OPTIONAL hooks for custom logic.
 * 🚀 You can even remove them all - notifications will still be sent automatically!
 */
@Component
public class AutomaticListeners {

    /**
     * 📧 EMAIL: High concurrency, immediate ACK
     * KafNotif AUTOMATICALLY sends via JavaMail after this hook
     */
    @KafNotifListener(
        value = NotificationType.EMAIL,
        concurrency = 10,
        ackMode = AckMode.MANUAL_IMMEDIATE,
        threadingMode = ThreadingMode.VIRTUAL_THREADS,
        groupId = "email-processor"
    )
    public void emailHook(EmailNotification email) {
        System.out.println("🔔 [BEFORE] Email will be auto-sent: " + email.getSubject());
        
        // Your optional custom logic:
        // - Validation, audit logging, rate limiting, etc.
        // - Then KafNotif automatically sends via JavaMail!
    }

    /**
     * 📱 SMS: Medium concurrency, manual ACK
     * NO METHOD NEEDED - KafNotif will automatically send via Twilio!
     */
    @KafNotifListener(
        value = NotificationType.SMS,
        concurrency = 5,
        ackMode = AckMode.MANUAL,
        threadingMode = ThreadingMode.VIRTUAL_THREADS,
        groupId = "sms-processor"
    )
    public void smsProcessor() {
        // This annotation is enough! SMS will be automatically sent via Twilio
        // You don't even need this method - just the annotation creates the consumer
    }

    /**
     * 🔔 PUSH: Low concurrency (FCM limits), platform threads
     * KafNotif AUTOMATICALLY sends via Firebase FCM after this hook
     */
    @KafNotifListener(
        value = NotificationType.PUSH,
        concurrency = 2,
        ackMode = AckMode.MANUAL_IMMEDIATE,
        threadingMode = ThreadingMode.PLATFORM_THREADS,
        groupId = "push-processor"
    )
    public void pushHook(PushNotification push) {
        System.out.println("🔔 [BEFORE] Push will be auto-sent: " + push.getTitle());
        
        // Then KafNotif automatically sends via Firebase FCM!
    }

    /**
     * 💬 SLACK: Auto ACK (fire and forget)
     * NO HOOK NEEDED - just automatic sending!
     */
    @KafNotifListener(
        value = NotificationType.SLACK,
        concurrency = 3,
        ackMode = AckMode.AUTO,
        threadingMode = ThreadingMode.VIRTUAL_THREADS,
        groupId = "slack-processor"
    )
    public void slackProcessor() {
        // Slack messages automatically sent via webhook - no code needed!
    }

    /**
     * 🎮 DISCORD: Manual ACK
     * NO HOOK NEEDED - just automatic sending!
     */
    @KafNotifListener(
        value = NotificationType.DISCORD,
        concurrency = 3,
        ackMode = AckMode.MANUAL,
        threadingMode = ThreadingMode.VIRTUAL_THREADS,
        groupId = "discord-processor"
    )
    public void discordProcessor() {
        // Discord messages automatically sent via webhook - no code needed!
    }

    /**
     * 🌐 WEBHOOK: High concurrency for web services
     * KafNotif AUTOMATICALLY makes HTTP calls after this hook
     */
    @KafNotifListener(
        value = NotificationType.WEBHOOK,
        concurrency = 7,
        ackMode = AckMode.MANUAL,
        threadingMode = ThreadingMode.VIRTUAL_THREADS,
        groupId = "webhook-processor"
    )
    public void webhookHook(WebhookNotification webhook) {
        System.out.println("🔔 [BEFORE] Webhook will be auto-called: " + webhook.getMethod() + " " + webhook.getUrl());
        
        // Then KafNotif automatically makes the HTTP call!
    }

    /*
     * 🎯 THAT'S IT! 
     * 
     * With just these annotations:
     * ✅ Different consumers are created with optimal settings per type
     * ✅ All notifications are automatically sent using concrete implementations
     * ✅ You can add optional hooks for custom logic
     * ✅ Or remove all methods - automatic sending still works!
     * 
     * The sending logic (JavaMail, Twilio, Firebase, etc.) is handled by KafNotif!
     */
}
