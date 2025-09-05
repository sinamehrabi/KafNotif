package com.kafnotif.notifier;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import com.kafnotif.model.NotificationType;
import com.kafnotif.notifier.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.Session;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to easily set up and register notifier implementations
 */
public class NotifierSetup {
    
    private static final Logger logger = LoggerFactory.getLogger(NotifierSetup.class);
    
    /**
     * Setup and register JavaMail email notifier
     */
    public static void setupEmailNotifier(String smtpHost, int smtpPort, String username, String password, String fromEmail) {
        try {
            JavaMailEmailNotifier emailNotifier = new JavaMailEmailNotifier(smtpHost, smtpPort, username, password, fromEmail);
            NotifierFactory.registerNotifier(NotificationType.EMAIL, emailNotifier);
            logger.info("Email notifier registered successfully");
        } catch (Exception e) {
            logger.error("Failed to setup email notifier", e);
            throw new RuntimeException("Email notifier setup failed", e);
        }
    }
    
    /**
     * Setup and register JavaMail email notifier with existing Session
     */
    public static void setupEmailNotifier(Session session, String fromEmail) {
        try {
            JavaMailEmailNotifier emailNotifier = new JavaMailEmailNotifier(session, fromEmail);
            NotifierFactory.registerNotifier(NotificationType.EMAIL, emailNotifier);
            logger.info("Email notifier registered successfully");
        } catch (Exception e) {
            logger.error("Failed to setup email notifier", e);
            throw new RuntimeException("Email notifier setup failed", e);
        }
    }
    
    /**
     * Setup and register Firebase push notifier with service account file
     */
    public static void setupFirebasePushNotifier(String serviceAccountPath) {
        try {
            FirebasePushNotifier pushNotifier = new FirebasePushNotifier(serviceAccountPath);
            NotifierFactory.registerNotifier(NotificationType.PUSH, pushNotifier);
            logger.info("Firebase push notifier registered successfully with service account: {}", serviceAccountPath);
        } catch (Exception e) {
            logger.error("Failed to setup Firebase push notifier with service account: {}", serviceAccountPath, e);
            throw new RuntimeException("Firebase push notifier setup failed", e);
        }
    }
    
    /**
     * Setup and register Firebase push notifier with service account InputStream
     */
    public static void setupFirebasePushNotifier(InputStream serviceAccountStream) {
        try {
            FirebasePushNotifier pushNotifier = new FirebasePushNotifier(serviceAccountStream);
            NotifierFactory.registerNotifier(NotificationType.PUSH, pushNotifier);
            logger.info("Firebase push notifier registered successfully with service account stream");
        } catch (Exception e) {
            logger.error("Failed to setup Firebase push notifier with service account stream", e);
            throw new RuntimeException("Firebase push notifier setup failed", e);
        }
    }
    
    /**
     * Setup and register Firebase push notifier with default app
     */
    public static void setupFirebasePushNotifier() {
        try {
            FirebasePushNotifier pushNotifier = new FirebasePushNotifier();
            NotifierFactory.registerNotifier(NotificationType.PUSH, pushNotifier);
            logger.info("Firebase push notifier registered successfully");
        } catch (Exception e) {
            logger.error("Failed to setup Firebase push notifier", e);
            throw new RuntimeException("Firebase push notifier setup failed", e);
        }
    }
    
    /**
     * Setup and register Twilio SMS notifier
     */
    public static void setupTwilioSmsNotifier(String accountSid, String authToken, String fromPhoneNumber) {
        try {
            TwilioSmsNotifier smsNotifier = new TwilioSmsNotifier(accountSid, authToken, fromPhoneNumber);
            NotifierFactory.registerNotifier(NotificationType.SMS, smsNotifier);
            logger.info("Twilio SMS notifier registered successfully");
        } catch (Exception e) {
            logger.error("Failed to setup Twilio SMS notifier", e);
            throw new RuntimeException("Twilio SMS notifier setup failed", e);
        }
    }
    
    /**
     * Setup and register Slack webhook notifier
     */
    public static void setupSlackNotifier(String webhookUrl) {
        try {
            SlackWebhookNotifier slackNotifier = new SlackWebhookNotifier(webhookUrl);
            NotifierFactory.registerNotifier(NotificationType.SLACK, slackNotifier);
            logger.info("Slack notifier registered successfully");
        } catch (Exception e) {
            logger.error("Failed to setup Slack notifier", e);
            throw new RuntimeException("Slack notifier setup failed", e);
        }
    }
    
    /**
     * Setup and register Discord webhook notifier
     */
    public static void setupDiscordNotifier(String webhookUrl) {
        try {
            DiscordWebhookNotifier discordNotifier = new DiscordWebhookNotifier(webhookUrl);
            NotifierFactory.registerNotifier(NotificationType.DISCORD, discordNotifier);
            logger.info("Discord notifier registered successfully");
        } catch (Exception e) {
            logger.error("Failed to setup Discord notifier", e);
            throw new RuntimeException("Discord notifier setup failed", e);
        }
    }
    
    /**
     * Setup and register HTTP webhook notifier
     */
    public static void setupWebhookNotifier() {
        try {
            HttpClientWebhookNotifier webhookNotifier = new HttpClientWebhookNotifier();
            NotifierFactory.registerNotifier(NotificationType.WEBHOOK, webhookNotifier);
            logger.info("Webhook notifier registered successfully");
        } catch (Exception e) {
            logger.error("Failed to setup webhook notifier", e);
            throw new RuntimeException("Webhook notifier setup failed", e);
        }
    }
    
    /**
     * Setup Gmail SMTP email notifier (convenience method)
     */
    public static void setupGmailNotifier(String email, String appPassword) {
        setupEmailNotifier("smtp.gmail.com", 587, email, appPassword, email);
    }
    
    /**
     * Setup all notifiers from environment variables
     */
    public static void setupFromEnvironment() {
        // Email setup
        String smtpHost = System.getenv("KAFNOTIF_SMTP_HOST");
        String smtpPort = System.getenv("KAFNOTIF_SMTP_PORT");
        String smtpUsername = System.getenv("KAFNOTIF_SMTP_USERNAME");
        String smtpPassword = System.getenv("KAFNOTIF_SMTP_PASSWORD");
        String fromEmail = System.getenv("KAFNOTIF_FROM_EMAIL");
        
        if (smtpHost != null && smtpPort != null && smtpUsername != null && smtpPassword != null && fromEmail != null) {
            setupEmailNotifier(smtpHost, Integer.parseInt(smtpPort), smtpUsername, smtpPassword, fromEmail);
        }
        
        // Firebase setup
        String firebaseServiceAccount = System.getenv("KAFNOTIF_FIREBASE_SERVICE_ACCOUNT");
        if (firebaseServiceAccount != null) {
            setupFirebasePushNotifier(firebaseServiceAccount);
        }
        
        // Twilio setup
        String twilioAccountSid = System.getenv("KAFNOTIF_TWILIO_ACCOUNT_SID");
        String twilioAuthToken = System.getenv("KAFNOTIF_TWILIO_AUTH_TOKEN");
        String twilioFromPhone = System.getenv("KAFNOTIF_TWILIO_FROM_PHONE");
        
        if (twilioAccountSid != null && twilioAuthToken != null && twilioFromPhone != null) {
            setupTwilioSmsNotifier(twilioAccountSid, twilioAuthToken, twilioFromPhone);
        }
        
        // Slack setup
        String slackWebhookUrl = System.getenv("KAFNOTIF_SLACK_WEBHOOK_URL");
        if (slackWebhookUrl != null) {
            setupSlackNotifier(slackWebhookUrl);
        }
        
        // Discord setup
        String discordWebhookUrl = System.getenv("KAFNOTIF_DISCORD_WEBHOOK_URL");
        if (discordWebhookUrl != null) {
            setupDiscordNotifier(discordWebhookUrl);
        }
        
        // Always setup webhook notifier
        setupWebhookNotifier();
        
        logger.info("Notifier setup from environment completed");
    }
}
