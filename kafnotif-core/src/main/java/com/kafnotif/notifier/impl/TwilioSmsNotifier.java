package com.kafnotif.notifier.impl;

import com.kafnotif.model.Event;
import com.kafnotif.model.SmsNotification;
import com.kafnotif.notifier.SmsNotifier;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Twilio implementation of SmsNotifier
 */
public class TwilioSmsNotifier implements SmsNotifier {
    
    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsNotifier.class);
    
    private final String fromPhoneNumber;
    
    /**
     * Constructor with Twilio credentials
     */
    public TwilioSmsNotifier(String accountSid, String authToken, String fromPhoneNumber) {
        Twilio.init(accountSid, authToken);
        this.fromPhoneNumber = fromPhoneNumber;
    }
    
    @Override
    public boolean sendSms(SmsNotification notification) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(notification.getRecipient()),
                    new PhoneNumber(fromPhoneNumber),
                    notification.getMessage())
                .create();
            
            logger.info("SMS sent successfully. SID: {}, Status: {}", 
                       message.getSid(), message.getStatus());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send SMS to: {}", notification.getRecipient(), e);
            return false;
        }
    }
    
    @Override
    public boolean sendSimpleSms(String phoneNumber, String message) {
        SmsNotification notification = new SmsNotification();
        notification.setRecipient(phoneNumber);
        notification.setMessage(message);
        return sendSms(notification);
    }
    
    @Override
    public boolean sendSmsWithCountryCode(String phoneNumber, String message, String countryCode) {
        String fullPhoneNumber = countryCode + phoneNumber;
        return sendSimpleSms(fullPhoneNumber, message);
    }
    
    @Override
    public void send(Event event) {
        if (event instanceof SmsNotification) {
            sendSms((SmsNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be an SmsNotification");
        }
    }
}
