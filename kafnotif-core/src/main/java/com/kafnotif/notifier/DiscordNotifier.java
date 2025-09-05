package com.kafnotif.notifier;

import com.kafnotif.model.DiscordNotification;
import com.kafnotif.model.Event;

/**
 * Discord-specific notifier interface
 */
public interface DiscordNotifier extends Notifier {
    
    /**
     * Send a Discord notification
     * @param notification the Discord notification to send
     * @return true if sent successfully, false otherwise
     */
    boolean sendDiscordMessage(DiscordNotification notification);
    
    /**
     * Send a simple Discord message
     * @param webhookUrl Discord webhook URL
     * @param content message content
     * @return true if sent successfully, false otherwise
     */
    boolean sendSimpleMessage(String webhookUrl, String content);
    
    /**
     * Send Discord message with embeds
     * @param notification the Discord notification with embeds
     * @return true if sent successfully, false otherwise
     */
    boolean sendMessageWithEmbeds(DiscordNotification notification);
    
    /**
     * Send Discord message with custom username and avatar
     * @param webhookUrl Discord webhook URL
     * @param content message content
     * @param username custom username
     * @param avatarUrl custom avatar URL
     * @return true if sent successfully, false otherwise
     */
    boolean sendCustomMessage(String webhookUrl, String content, String username, String avatarUrl);
    
    @Override
    default void send(Event event) {
        if (event instanceof DiscordNotification) {
            sendDiscordMessage((DiscordNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be a DiscordNotification");
        }
    }
}
