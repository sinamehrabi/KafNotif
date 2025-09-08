package com.kafnotif.examples.automatic;

import com.kafnotif.model.EmailNotification;
import com.kafnotif.model.NotificationType;
import com.kafnotif.spring.KafNotifListener;
import com.kafnotif.consumer.AckMode;
import com.kafnotif.hooks.AckControl;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Simple example showing database integration with email hooks
 * Demonstrates common patterns for storing email status
 */
@Component
public class DatabaseEmailHooks {

    // Simulated database (use real JPA/database in production)
    private final Map<String, EmailRecord> emailDatabase = new ConcurrentHashMap<>();

    @KafNotifListener(
        value = NotificationType.EMAIL,
        ackMode = AckMode.MANUAL,
        concurrency = 3
    )
    public void handleEmailWithDatabase(EmailNotification email, AckControl ackControl) {
        String emailId = email.getId();
        
        try {
            // BEFORE SEND: Store email attempt
            EmailRecord record = new EmailRecord(
                emailId,
                email.getRecipient(),
                email.getSubject(),
                "PROCESSING",
                LocalDateTime.now(),
                null,
                0
            );
            emailDatabase.put(emailId, record);
            
            System.out.println("💾 [BEFORE] Stored email record: " + emailId);
            
            // Validation
            if (!isValidForSending(email)) {
                updateEmailRecord(emailId, "INVALID", "Email validation failed");
                ackControl.acknowledge(); // Skip invalid emails
                return;
            }
            
            // KafNotif will automatically send the email
            // We'll update the status in a separate afterSend hook or use the comprehensive hooks approach
            
        } catch (Exception e) {
            System.out.println("💥 Database error: " + e.getMessage());
            updateEmailRecord(emailId, "ERROR", e.getMessage());
            // Don't acknowledge - let it retry
        }
    }

    // You can also create a separate method for after-send updates
    // This would be called by implementing NotificationHooks interface
    public void updateAfterSend(EmailNotification email, boolean success, Throwable error) {
        String emailId = email.getId();
        
        if (success) {
            updateEmailRecord(emailId, "SENT", "Successfully delivered");
            System.out.println("✅ [AFTER] Email " + emailId + " marked as SENT");
        } else {
            String errorMsg = error != null ? error.getMessage() : "Unknown error";
            updateEmailRecord(emailId, "FAILED", errorMsg);
            System.out.println("❌ [AFTER] Email " + emailId + " marked as FAILED: " + errorMsg);
        }
    }

    private boolean isValidForSending(EmailNotification email) {
        // Business validation rules
        if (email.getRecipient() == null || !email.getRecipient().contains("@")) {
            return false;
        }
        
        if (email.getSubject() == null || email.getSubject().trim().isEmpty()) {
            return false;
        }
        
        // Check if recipient is blacklisted
        if (isBlacklisted(email.getRecipient())) {
            return false;
        }
        
        return true;
    }

    private boolean isBlacklisted(String recipient) {
        // Simple blacklist check
        return recipient.contains("spam") || 
               recipient.contains("noreply") ||
               recipient.endsWith("@blocked.com");
    }

    private void updateEmailRecord(String emailId, String status, String message) {
        EmailRecord record = emailDatabase.get(emailId);
        if (record != null) {
            record.status = status;
            record.lastUpdated = LocalDateTime.now();
            record.message = message;
            record.attempts++;
            
            System.out.println("📊 Updated email " + emailId + ": " + status + " - " + message);
        }
    }

    // Get email status (for API endpoints, monitoring, etc.)
    public EmailRecord getEmailStatus(String emailId) {
        return emailDatabase.get(emailId);
    }

    // Get all emails with specific status
    public Map<String, EmailRecord> getEmailsByStatus(String status) {
        return emailDatabase.entrySet().stream()
            .filter(entry -> status.equals(entry.getValue().status))
            .collect(ConcurrentHashMap::new, 
                    (map, entry) -> map.put(entry.getKey(), entry.getValue()), 
                    ConcurrentHashMap::putAll);
    }

    // Simple record class for email tracking
    public static class EmailRecord {
        public String id;
        public String recipient;
        public String subject;
        public String status;
        public LocalDateTime created;
        public LocalDateTime lastUpdated;
        public String message;
        public int attempts;

        public EmailRecord(String id, String recipient, String subject, String status, 
                          LocalDateTime created, String message, int attempts) {
            this.id = id;
            this.recipient = recipient;
            this.subject = subject;
            this.status = status;
            this.created = created;
            this.lastUpdated = created;
            this.message = message;
            this.attempts = attempts;
        }

        @Override
        public String toString() {
            return String.format("EmailRecord{id='%s', recipient='%s', status='%s', attempts=%d}", 
                                id, recipient, status, attempts);
        }
    }
}
