package com.kafnotif.examples.automatic;

import com.kafnotif.hooks.AckControl;
import com.kafnotif.hooks.NotificationHooks;
import com.kafnotif.model.EmailNotification;
import com.kafnotif.model.NotificationEvent;
import org.springframework.stereotype.Component;

/**
 * Example of comprehensive email lifecycle hooks
 * Demonstrates beforeSend, afterSend, onRetry, and onPermanentFailure
 */
@Component
public class EmailLifecycleHooks implements NotificationHooks {

    @Override
    public boolean beforeSend(NotificationEvent notification, AckControl ackControl) {
        if (notification instanceof EmailNotification email) {
            System.out.println("🔔 [BEFORE SEND] Email: " + email.getSubject() + " → " + email.getRecipient());
            
            // Custom business logic
            try {
                // 1. Validate email
                if (!isValidEmail(email)) {
                    System.out.println("❌ Invalid email, skipping");
                    ackControl.acknowledge(); // Skip invalid emails
                    return false; // Don't proceed with sending
                }
                
                // 2. Store in database
                saveEmailToDatabase(email, "SENDING");
                
                // 3. Apply business rules
                if (isBlacklisted(email.getRecipient())) {
                    System.out.println("🚫 Recipient blacklisted, skipping");
                    updateEmailStatus(email, "BLOCKED");
                    ackControl.acknowledge();
                    return false;
                }
                
                // 4. Rate limiting check
                if (isRateLimited(email.getRecipient())) {
                    System.out.println("⏳ Rate limited, will retry later");
                    return false; // Don't acknowledge - will retry
                }
                
                System.out.println("✅ Pre-send validation passed");
                return true; // Proceed with sending
                
            } catch (Exception e) {
                System.out.println("💥 Error in beforeSend: " + e.getMessage());
                return false; // Don't proceed if validation fails
            }
        }
        return true; // Continue for other notification types
    }

    @Override
    public void afterSend(NotificationEvent notification, boolean success, Throwable error, AckControl ackControl) {
        if (notification instanceof EmailNotification email) {
            if (success) {
                System.out.println("✅ [AFTER SEND SUCCESS] Email sent: " + email.getSubject());
                
                // Success logic
                updateEmailStatus(email, "SENT");
                recordSuccessMetrics(email);
                sendSuccessNotification(email);
                
                // Note: KafNotif will auto-acknowledge here if not done manually
                
            } else {
                System.out.println("❌ [AFTER SEND FAILURE] Email failed: " + email.getSubject());
                System.out.println("   Error: " + (error != null ? error.getMessage() : "Unknown"));
                
                // Failure logic
                updateEmailStatus(email, "FAILED");
                recordFailureMetrics(email, error);
                
                // Don't acknowledge - let retry mechanism handle it
            }
        }
    }

    @Override
    public void onRetry(NotificationEvent notification, int retryAttempt, int maxRetries) {
        if (notification instanceof EmailNotification email) {
            System.out.println("🔄 [RETRY " + retryAttempt + "/" + maxRetries + "] Email: " + email.getSubject());
            
            // Retry logic
            updateEmailStatus(email, "RETRYING_" + retryAttempt);
            
            // Optional: Log retry information
            if (retryAttempt > 2) {
                System.out.println("   Multiple retries detected, may need investigation...");
            }
        }
    }

    @Override
    public void onPermanentFailure(NotificationEvent notification, Throwable lastError, AckControl ackControl) {
        if (notification instanceof EmailNotification email) {
            System.out.println("💀 [PERMANENT FAILURE] Email: " + email.getSubject());
            System.out.println("   Final error: " + (lastError != null ? lastError.getMessage() : "Unknown"));
            
            // Permanent failure logic
            updateEmailStatus(email, "PERMANENTLY_FAILED");
            sendFailureAlert(email, lastError);
            moveToDeadLetterQueue(email);
            
            // Note: Message will be acknowledged automatically to prevent infinite retries
        }
    }

    // Helper methods for business logic
    private boolean isValidEmail(EmailNotification email) {
        return email.getRecipient() != null && 
               email.getRecipient().contains("@") &&
               email.getSubject() != null &&
               !email.getSubject().trim().isEmpty();
    }

    private boolean isBlacklisted(String email) {
        // Check against blacklist
        return email.contains("spam") || email.contains("blocked");
    }

    private boolean isRateLimited(String email) {
        // Check rate limiting rules
        // Return true if should be rate limited
        return false; // Simplified for example
    }


    private void saveEmailToDatabase(EmailNotification email, String status) {
        System.out.println("💾 Saving email " + email.getId() + " with status: " + status);
        // Your database logic here
    }

    private void updateEmailStatus(EmailNotification email, String status) {
        System.out.println("📊 Email " + email.getId() + " status updated to: " + status);
        // Update database, send webhooks, etc.
    }

    private void recordSuccessMetrics(EmailNotification email) {
        System.out.println("📈 Recording success metrics for email type");
        // Send to monitoring system
    }

    private void recordFailureMetrics(EmailNotification email, Throwable error) {
        System.out.println("📉 Recording failure metrics: " + (error != null ? error.getClass().getSimpleName() : "Unknown"));
        // Send to monitoring system
    }

    private void sendSuccessNotification(EmailNotification email) {
        System.out.println("🔔 Sending success webhook/notification");
        // Notify other systems of successful delivery
    }

    private void sendFailureAlert(EmailNotification email, Throwable error) {
        System.out.println("🚨 Sending failure alert to administrators");
        // Alert ops team about permanent failures
    }

    private void moveToDeadLetterQueue(EmailNotification email) {
        System.out.println("☠️ Moving email to dead letter queue for manual review");
        // Store for manual investigation
    }
}
