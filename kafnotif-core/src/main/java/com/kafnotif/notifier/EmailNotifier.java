package com.kafnotif.notifier;

import com.kafnotif.model.EmailNotification;
import com.kafnotif.model.Event;

/**
 * Email-specific notifier interface
 */
public interface EmailNotifier extends Notifier {
    
    /**
     * Send an email notification
     * @param notification the email notification to send
     * @return true if sent successfully, false otherwise
     */
    boolean sendEmail(EmailNotification notification);
    
    /**
     * Send a simple email
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     * @return true if sent successfully, false otherwise
     */
    boolean sendSimpleEmail(String to, String subject, String body);
    
    /**
     * Send an HTML email
     * @param to recipient email address
     * @param subject email subject
     * @param htmlBody HTML email body
     * @return true if sent successfully, false otherwise
     */
    boolean sendHtmlEmail(String to, String subject, String htmlBody);
    
    @Override
    default void send(Event event) {
        if (event instanceof EmailNotification) {
            sendEmail((EmailNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be an EmailNotification");
        }
    }
}
