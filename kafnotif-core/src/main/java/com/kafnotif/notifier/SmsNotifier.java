package com.kafnotif.notifier;

import com.kafnotif.model.SmsNotification;
import com.kafnotif.model.Event;

/**
 * SMS-specific notifier interface
 */
public interface SmsNotifier extends Notifier {
    
    /**
     * Send an SMS notification
     * @param notification the SMS notification to send
     * @return true if sent successfully, false otherwise
     */
    boolean sendSms(SmsNotification notification);
    
    /**
     * Send a simple SMS
     * @param phoneNumber recipient phone number
     * @param message SMS message
     * @return true if sent successfully, false otherwise
     */
    boolean sendSimpleSms(String phoneNumber, String message);
    
    /**
     * Send SMS with country code
     * @param phoneNumber recipient phone number
     * @param message SMS message
     * @param countryCode country code (e.g., "+1")
     * @return true if sent successfully, false otherwise
     */
    boolean sendSmsWithCountryCode(String phoneNumber, String message, String countryCode);
    
    @Override
    default void send(Event event) {
        if (event instanceof SmsNotification) {
            sendSms((SmsNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be an SmsNotification");
        }
    }
}
