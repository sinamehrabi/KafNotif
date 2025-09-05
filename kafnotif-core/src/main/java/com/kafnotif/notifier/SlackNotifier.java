package com.kafnotif.notifier;

import com.kafnotif.model.SlackNotification;
import com.kafnotif.model.Event;

/**
 * Slack-specific notifier interface
 */
public interface SlackNotifier extends Notifier {
    
    /**
     * Send a Slack notification
     * @param notification the Slack notification to send
     * @return true if sent successfully, false otherwise
     */
    boolean sendSlackMessage(SlackNotification notification);
    
    /**
     * Send a simple Slack message
     * @param channel channel to send message to
     * @param text message text
     * @return true if sent successfully, false otherwise
     */
    boolean sendSimpleMessage(String channel, String text);
    
    /**
     * Send Slack message with attachments
     * @param notification the Slack notification with attachments
     * @return true if sent successfully, false otherwise
     */
    boolean sendMessageWithAttachments(SlackNotification notification);
    
    /**
     * Send Slack message to thread
     * @param channel channel to send message to
     * @param text message text
     * @param threadTs thread timestamp to reply to
     * @return true if sent successfully, false otherwise
     */
    boolean sendThreadMessage(String channel, String text, String threadTs);
    
    @Override
    default void send(Event event) {
        if (event instanceof SlackNotification) {
            sendSlackMessage((SlackNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be a SlackNotification");
        }
    }
}
