package com.example.demo.listeners;

import com.kafnotif.config.ThreadingMode;
import com.kafnotif.consumer.AckMode;
import com.kafnotif.model.NotificationType;
import com.kafnotif.model.SlackNotification;
import com.kafnotif.spring.KafNotifListener;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class SlackNotificationListener {

    @KafNotifListener(
            value = NotificationType.SLACK,
            concurrency = 3,
            ackMode = AckMode.AUTO,
            threadingMode = ThreadingMode.VIRTUAL_THREADS
    )
    public void slackHandler(SlackNotification slack) {
        System.out.println("💬 [SLACK] Message sent to #" + slack.getChannel() + ": " + slack.getText());
        System.out.println("    Username: " + slack.getUsername());
        // KafNotif automatically sends the Slack message and acknowledges
    }
}
