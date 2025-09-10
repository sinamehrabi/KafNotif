package com.example.demo.listeners;

import com.kafnotif.config.ThreadingMode;
import com.kafnotif.consumer.AckMode;
import com.kafnotif.hooks.AckControl;
import com.kafnotif.model.EmailNotification;
import com.kafnotif.model.NotificationType;
import com.kafnotif.spring.KafNotifListener;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class EmailNotificationListener {

    @KafNotifListener(
            value = NotificationType.EMAIL,
            concurrency = 5,
            ackMode = AckMode.MANUAL,
            threadingMode = ThreadingMode.VIRTUAL_THREADS,
            afterSend = "emailAfterHook"
    )
    public void emailBeforeHook(EmailNotification email) {
        System.out.println("🔔 [BEFORE] Email will be auto-sent: " + email.getSubject() + " -> " + email.getRecipient());
    }

    public void emailAfterHook(EmailNotification email, boolean success, Exception error, AckControl ackControl) {
        if (success) {
            System.out.println("✅ Email sent successfully: " + email.getSubject() + " -> " + email.getRecipient());
            ackControl.acknowledge();
            // Add your custom business logic here (database updates, analytics, etc.)
        } else {
            System.out.println("❌ Email failed: " + email.getSubject() + " -> " + email.getRecipient() + 
                             " Error: " + (error != null ? error.getMessage() : "Unknown error"));
            // Handle failures - decide whether to retry or acknowledge
            ackControl.acknowledge(); // Skip failed message for this example
        }
    }
}
