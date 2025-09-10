package com.kafnotif.spring;

import com.kafnotif.model.NotificationType;
import com.kafnotif.notifier.NotifierFactory;
import com.kafnotif.notifier.impl.*;
import com.kafnotif.notifier.impl.MultiChannelSlackWebhookNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Automatically sets up concrete notifier implementations based on configuration
 */
@Component
@ConfigurationProperties(prefix = "kafnotif.providers")
public class AutomaticNotifierSetup {
    
    private static final Logger logger = LoggerFactory.getLogger(AutomaticNotifierSetup.class);
    
    // Email configuration
    private EmailConfig email = new EmailConfig();
    
    // Firebase configuration  
    private FirebaseConfig firebase = new FirebaseConfig();
    
    // Twilio configuration
    private TwilioConfig twilio = new TwilioConfig();
    
    // Slack configuration
    private SlackConfig slack = new SlackConfig();
    
    // Discord configuration
    private DiscordConfig discord = new DiscordConfig();
    
    // Webhook configuration (always enabled)
    private boolean enableWebhooks = true;
    
    @PostConstruct
    public void setupAutomaticNotifiers() {
        logger.info("🚀 Setting up automatic concrete notifiers...");
        
        // Setup email notifier if configured
        if (email.isEnabled()) {
            setupEmailNotifier();
        } else {
            setupConsoleEmailNotifier();
        }
        
        // Setup Firebase push notifier if configured
        if (firebase.isEnabled()) {
            setupFirebaseNotifier();
        } else {
            setupConsolePushNotifier();
        }
        
        // Setup Twilio SMS notifier if configured
        if (twilio.isEnabled()) {
            setupTwilioNotifier();
        } else {
            setupConsoleSmsNotifier();
        }
        
        // Setup Slack notifier if configured
        if (slack.isEnabled()) {
            setupSlackNotifier();
        } else {
            setupConsoleSlackNotifier();
        }
        
        // Setup Discord notifier if configured
        if (discord.isEnabled()) {
            setupDiscordNotifier();
        } else {
            setupConsoleDiscordNotifier();
        }
        
        // Setup webhook notifier (always enabled)
        if (enableWebhooks) {
            setupWebhookNotifier();
        } else {
            setupConsoleWebhookNotifier();
        }
        
        logger.info("✅ Automatic notifier setup complete!");
    }
    
    private void setupEmailNotifier() {
        try {
            JavaMailEmailNotifier emailNotifier = new JavaMailEmailNotifier(
                email.getSmtpHost(), email.getSmtpPort(), 
                email.getUsername(), email.getPassword(), 
                email.getFromEmail()
            );
            NotifierFactory.registerNotifier(NotificationType.EMAIL, emailNotifier);
            logger.info("📧 JavaMail email notifier registered");
        } catch (Exception e) {
            logger.error("Failed to setup JavaMail notifier, using console fallback", e);
            setupConsoleEmailNotifier();
        }
    }
    
    private void setupFirebaseNotifier() {
        try {
            FirebasePushNotifier pushNotifier = new FirebasePushNotifier(firebase.getServiceAccountPath());
            NotifierFactory.registerNotifier(NotificationType.PUSH, pushNotifier);
            logger.info("🔔 Firebase push notifier registered");
        } catch (Exception e) {
            logger.error("Failed to setup Firebase notifier, using console fallback", e);
            setupConsolePushNotifier();
        }
    }
    
    private void setupTwilioNotifier() {
        try {
            TwilioSmsNotifier smsNotifier = new TwilioSmsNotifier(
                twilio.getAccountSid(), twilio.getAuthToken(), twilio.getFromPhone()
            );
            NotifierFactory.registerNotifier(NotificationType.SMS, smsNotifier);
            logger.info("📱 Twilio SMS notifier registered");
        } catch (Exception e) {
            logger.error("Failed to setup Twilio notifier, using console fallback", e);
            setupConsoleSmsNotifier();
        }
    }
    
    private void setupSlackNotifier() {
        try {
            MultiChannelSlackWebhookNotifier slackNotifier = new MultiChannelSlackWebhookNotifier(slack);
            NotifierFactory.registerNotifier(NotificationType.SLACK, slackNotifier);
            
            if (!slack.getChannels().isEmpty()) {
                logger.info("💬 Multi-channel Slack webhook notifier registered with {} channels: {}", 
                    slack.getChannels().size(), slack.getChannels().keySet());
            } else {
                logger.info("💬 Slack webhook notifier registered (single webhook mode)");
            }
        } catch (Exception e) {
            logger.error("Failed to setup Slack notifier, using console fallback", e);
            setupConsoleSlackNotifier();
        }
    }
    
    private void setupDiscordNotifier() {
        try {
            DiscordWebhookNotifier discordNotifier = new DiscordWebhookNotifier(discord.getWebhookUrl());
            NotifierFactory.registerNotifier(NotificationType.DISCORD, discordNotifier);
            logger.info("🎮 Discord webhook notifier registered");
        } catch (Exception e) {
            logger.error("Failed to setup Discord notifier, using console fallback", e);
            setupConsoleDiscordNotifier();
        }
    }
    
    private void setupWebhookNotifier() {
        try {
            HttpClientWebhookNotifier webhookNotifier = new HttpClientWebhookNotifier();
            NotifierFactory.registerNotifier(NotificationType.WEBHOOK, webhookNotifier);
            logger.info("🌐 HTTP webhook notifier registered");
        } catch (Exception e) {
            logger.error("Failed to setup webhook notifier, using console fallback", e);
            setupConsoleWebhookNotifier();
        }
    }
    
    // Console fallback notifiers for demo/development
    private void setupConsoleEmailNotifier() {
        NotifierFactory.registerNotifier(NotificationType.EMAIL, event -> {
            var email = (com.kafnotif.model.EmailNotification) event;
            System.out.println("📧 [CONSOLE] Sending Email: " + email.getSubject() + " -> " + email.getRecipient());
            System.out.println("   Body: " + email.getBody());
        });
        logger.info("📧 Console email notifier registered (fallback)");
    }
    
    private void setupConsolePushNotifier() {
        NotifierFactory.registerNotifier(NotificationType.PUSH, event -> {
            var push = (com.kafnotif.model.PushNotification) event;
            System.out.println("🔔 [CONSOLE] Sending Push: " + push.getTitle() + " -> " + push.getDeviceToken());
            System.out.println("   Body: " + push.getBody());
        });
        logger.info("🔔 Console push notifier registered (fallback)");
    }
    
    private void setupConsoleSmsNotifier() {
        NotifierFactory.registerNotifier(NotificationType.SMS, event -> {
            var sms = (com.kafnotif.model.SmsNotification) event;
            System.out.println("📱 [CONSOLE] Sending SMS: " + sms.getMessage() + " -> " + sms.getRecipient());
        });
        logger.info("📱 Console SMS notifier registered (fallback)");
    }
    
    private void setupConsoleSlackNotifier() {
        NotifierFactory.registerNotifier(NotificationType.SLACK, event -> {
            var slack = (com.kafnotif.model.SlackNotification) event;
            System.out.println("💬 [CONSOLE] Sending Slack: " + slack.getText() + " -> " + slack.getChannel());
        });
        logger.info("💬 Console Slack notifier registered (fallback)");
    }
    
    private void setupConsoleDiscordNotifier() {
        NotifierFactory.registerNotifier(NotificationType.DISCORD, event -> {
            var discord = (com.kafnotif.model.DiscordNotification) event;
            System.out.println("🎮 [CONSOLE] Sending Discord: " + discord.getContent());
        });
        logger.info("🎮 Console Discord notifier registered (fallback)");
    }
    
    private void setupConsoleWebhookNotifier() {
        NotifierFactory.registerNotifier(NotificationType.WEBHOOK, event -> {
            var webhook = (com.kafnotif.model.WebhookNotification) event;
            System.out.println("🌐 [CONSOLE] Calling Webhook: " + webhook.getMethod() + " " + webhook.getUrl());
        });
        logger.info("🌐 Console webhook notifier registered (fallback)");
    }
    
    // Configuration classes
    public static class EmailConfig {
        private boolean enabled = false;
        private String smtpHost = "smtp.gmail.com";
        private int smtpPort = 587;
        private String username;
        private String password;
        private String fromEmail;
        
        // Getters and setters
        public boolean isEnabled() { 
            // Allow email without authentication for development (e.g., MailCatcher)
            return enabled; 
        }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getSmtpHost() { return smtpHost; }
        public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }
        public int getSmtpPort() { return smtpPort; }
        public void setSmtpPort(int smtpPort) { this.smtpPort = smtpPort; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFromEmail() { return fromEmail; }
        public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }
    }
    
    public static class FirebaseConfig {
        private boolean enabled = false;
        private String serviceAccountPath;
        
        public boolean isEnabled() { return enabled && serviceAccountPath != null; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getServiceAccountPath() { return serviceAccountPath; }
        public void setServiceAccountPath(String serviceAccountPath) { this.serviceAccountPath = serviceAccountPath; }
    }
    
    public static class TwilioConfig {
        private boolean enabled = false;
        private String accountSid;
        private String authToken;
        private String fromPhone;
        
        public boolean isEnabled() { return enabled && accountSid != null && authToken != null; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAccountSid() { return accountSid; }
        public void setAccountSid(String accountSid) { this.accountSid = accountSid; }
        public String getAuthToken() { return authToken; }
        public void setAuthToken(String authToken) { this.authToken = authToken; }
        public String getFromPhone() { return fromPhone; }
        public void setFromPhone(String fromPhone) { this.fromPhone = fromPhone; }
    }
    
    public static class SlackConfig {
        private boolean enabled = false;
        private String defaultChannel = "general";
        private String webhookUrl; // Backward compatibility
        private Map<String, SlackChannelConfig> channels = new HashMap<>();
        
        public boolean isEnabled() { 
            return enabled && (webhookUrl != null || !channels.isEmpty()); 
        }
        
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        // Backward compatibility
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        
        // New multi-channel support
        public String getDefaultChannel() { return defaultChannel; }
        public void setDefaultChannel(String defaultChannel) { this.defaultChannel = defaultChannel; }
        
        public Map<String, SlackChannelConfig> getChannels() { return channels; }
        public void setChannels(Map<String, SlackChannelConfig> channels) { this.channels = channels; }
        
        /**
         * Get webhook URL for a specific channel
         */
        public String getWebhookUrlForChannel(String channel) {
            if (channels.containsKey(channel)) {
                return channels.get(channel).getWebhookUrl();
            }
            // Fallback to default webhook URL for backward compatibility
            return webhookUrl;
        }
        
        /**
         * Get default username for a specific channel
         */
        public String getDefaultUsernameForChannel(String channel) {
            if (channels.containsKey(channel)) {
                return channels.get(channel).getDefaultUsername();
            }
            return null; // No default username
        }
    }
    
    public static class SlackChannelConfig {
        private String webhookUrl;
        private String defaultUsername;
        
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        
        public String getDefaultUsername() { return defaultUsername; }
        public void setDefaultUsername(String defaultUsername) { this.defaultUsername = defaultUsername; }
    }
    
    public static class DiscordConfig {
        private boolean enabled = false;
        private String webhookUrl;
        
        public boolean isEnabled() { return enabled && webhookUrl != null; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    }
    
    // Getters and setters for main configs
    public EmailConfig getEmail() { return email; }
    public void setEmail(EmailConfig email) { this.email = email; }
    public FirebaseConfig getFirebase() { return firebase; }
    public void setFirebase(FirebaseConfig firebase) { this.firebase = firebase; }
    public TwilioConfig getTwilio() { return twilio; }
    public void setTwilio(TwilioConfig twilio) { this.twilio = twilio; }
    public SlackConfig getSlack() { return slack; }
    public void setSlack(SlackConfig slack) { this.slack = slack; }
    public DiscordConfig getDiscord() { return discord; }
    public void setDiscord(DiscordConfig discord) { this.discord = discord; }
    public boolean isEnableWebhooks() { return enableWebhooks; }
    public void setEnableWebhooks(boolean enableWebhooks) { this.enableWebhooks = enableWebhooks; }
}
