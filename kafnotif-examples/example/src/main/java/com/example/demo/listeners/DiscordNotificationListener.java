package com.example.demo.listeners;

import com.kafnotif.config.ThreadingMode;
import com.kafnotif.consumer.AckMode;
import com.kafnotif.model.DiscordNotification;
import com.kafnotif.model.NotificationType;
import com.kafnotif.spring.KafNotifListener;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class DiscordNotificationListener {

    @KafNotifListener(
            value = NotificationType.DISCORD,
            concurrency = 3,
            ackMode = AckMode.AUTO,
            threadingMode = ThreadingMode.VIRTUAL_THREADS
    )
    public void discordHandler(DiscordNotification discord) {
        String channel = discord.getChannelId() != null ? discord.getChannelId() : "unknown";
        System.out.println("🎮 [DISCORD] Message sent to #" + channel + ": " + discord.getContent());
        System.out.println("    Username: " + discord.getUsername());
        // KafNotif automatically sends the Discord message and acknowledges
    }
}
