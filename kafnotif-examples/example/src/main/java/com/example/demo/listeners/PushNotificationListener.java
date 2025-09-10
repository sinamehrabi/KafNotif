package com.example.demo.listeners;

import com.kafnotif.config.ThreadingMode;
import com.kafnotif.consumer.AckMode;
import com.kafnotif.hooks.AckControl;
import com.kafnotif.model.NotificationType;
import com.kafnotif.model.PushNotification;
import com.kafnotif.spring.KafNotifListener;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class PushNotificationListener {

    @KafNotifListener(
            value = NotificationType.PUSH,
            concurrency = 5,
            ackMode = AckMode.MANUAL,
            threadingMode = ThreadingMode.VIRTUAL_THREADS,
            afterSend = "pushAfterHook"
    )
    public void pushBeforeHook(PushNotification push) {
        System.out.println("🔔 [BEFORE] Push will be auto-sent: " + push.getTitle() + " -> " + push.getDeviceToken());
    }

    public void pushAfterHook(PushNotification push, boolean success, Exception error, AckControl ackControl) {
        if (success) {
            System.out.println("✅ Push sent successfully: " + push.getTitle() + " -> " + push.getDeviceToken());
            ackControl.acknowledge();
            // Add your custom business logic here (analytics, database updates, etc.)
        } else {
            System.out.println("❌ Push failed: " + push.getTitle() + " -> " + push.getDeviceToken() + 
                             " Error: " + (error != null ? error.getMessage() : "Unknown error"));
            // Handle failures - decide whether to retry or acknowledge
            ackControl.acknowledge(); // Skip failed message for this example
        }
    }
}
