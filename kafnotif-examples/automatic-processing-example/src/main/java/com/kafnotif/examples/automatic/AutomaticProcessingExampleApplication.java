package com.kafnotif.examples.automatic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 🤖 COMPLETELY AUTOMATIC NOTIFICATION PROCESSING!
 * 
 * This application shows the ULTIMATE SIMPLICITY:
 * 
 * 1. Developers add @KafNotifListener annotations
 * 2. KafNotif automatically handles ALL processing:
 *    - FCM push notifications
 *    - Email sending (JavaMail)
 *    - SMS delivery (Twilio)
 *    - Slack messages
 *    - Discord messages  
 *    - Webhook calls
 * 3. NO implementation code needed by developers!
 * 
 * Perfect for:
 * - Microservices that just need to process notifications
 * - Background workers
 * - Event-driven architectures
 * - Teams that want zero boilerplate
 */
@SpringBootApplication
public class AutomaticProcessingExampleApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AutomaticProcessingExampleApplication.class, args);
        
        System.out.println();
        System.out.println("🤖 AUTOMATIC NOTIFICATION PROCESSING STARTED!");
        System.out.println();
        System.out.println("✨ Completely automatic listeners active:");
        System.out.println("📧 EMAIL: Auto-sent via JavaMail (10 concurrency, MANUAL_IMMEDIATE ACK)");
        System.out.println("📱 SMS: Auto-sent via Twilio (5 concurrency, MANUAL ACK)");  
        System.out.println("🔔 PUSH: Auto-sent via FCM (2 concurrency, Platform threads)");
        System.out.println("💬 SLACK: Auto-sent via Webhook (3 concurrency, AUTO ACK)");
        System.out.println("🎮 DISCORD: Auto-sent via Webhook (3 concurrency, MANUAL ACK)");
        System.out.println("🌐 WEBHOOK: Auto-called via HTTP (7 concurrency, MANUAL ACK)");
        System.out.println("🤖 BATCH: Auto-processes multiple types (20 concurrency)");
        System.out.println("🔍 VALIDATED: Email validation before auto-processing");
        System.out.println("🎯 CUSTOM: Custom SMS processing example");
        System.out.println();
        System.out.println("🎉 Developers wrote ZERO implementation code!");
        System.out.println("📨 Send notifications and watch them auto-process! 🚀");
        System.out.println();
        System.out.println("Configure real providers:");
        System.out.println("- Set FIREBASE_SERVICE_ACCOUNT for FCM");
        System.out.println("- Set EMAIL_USERNAME/PASSWORD for JavaMail");
        System.out.println("- Set TWILIO_ACCOUNT_SID/AUTH_TOKEN for SMS");
        System.out.println("- Set SLACK_WEBHOOK_URL for Slack");
        System.out.println("- Set DISCORD_WEBHOOK_URL for Discord");
    }
}
