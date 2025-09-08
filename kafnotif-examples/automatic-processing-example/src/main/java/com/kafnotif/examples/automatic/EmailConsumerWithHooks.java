package com.kafnotif.examples.automatic;

import com.kafnotif.KafNotif;
import com.kafnotif.consumer.ConsumerConfig;
import com.kafnotif.consumer.NotificationConsumer;
import com.kafnotif.config.ThreadingMode;
import com.kafnotif.consumer.AckMode;
import com.kafnotif.model.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Set;

/**
 * Example of manually configuring a consumer with comprehensive hooks
 * Alternative to @KafNotifListener annotation approach
 */
@Component
public class EmailConsumerWithHooks {

    @Autowired
    private EmailLifecycleHooks emailHooks;

    private NotificationConsumer consumer;

    @PostConstruct
    public void startConsumer() {
        // Create consumer configuration with hooks
        ConsumerConfig config = new ConsumerConfig("email-hooks-group")
            .bootstrapServers("localhost:9092")
            .topicPrefix("notifications")
            .notificationTypes(Set.of(NotificationType.EMAIL))
            .threadingMode(ThreadingMode.VIRTUAL_THREADS)
            .concurrency(3)
            .ackMode(AckMode.MANUAL)
            .maxRetries(5)
            .enableDlq(true)
            .hooks(emailHooks); // Register your comprehensive hooks

        // Create and start consumer
        consumer = new NotificationConsumer(config);
        consumer.start();

        System.out.println("✅ Email consumer with comprehensive hooks started");
    }

    @PreDestroy
    public void stopConsumer() {
        if (consumer != null) {
            consumer.stop();
            System.out.println("🛑 Email consumer with hooks stopped");
        }
    }
}
