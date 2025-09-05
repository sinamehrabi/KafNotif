package com.kafnotif.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SMS notification specific implementation
 */
public class SmsNotification extends NotificationEvent {
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("countryCode")
    private String countryCode;
    
    @JsonProperty("provider")
    private String provider; // e.g., "twilio", "aws-sns", "nexmo"

    public SmsNotification() {
        super();
    }

    public SmsNotification(String phoneNumber, String message) {
        super(NotificationType.SMS, phoneNumber);
        this.message = message;
    }

    public SmsNotification(String phoneNumber, String message, String countryCode) {
        this(phoneNumber, message);
        this.countryCode = countryCode;
    }

    @Override
    public boolean isValid() {
        return getRecipient() != null && !getRecipient().trim().isEmpty() &&
               message != null && !message.trim().isEmpty() &&
               message.length() <= 1600 && // SMS length limit
               isValidPhoneNumber(getRecipient());
    }

    @Override
    public String getContent() {
        return message;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Basic phone number validation - can be enhanced based on requirements
        return phoneNumber != null && phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
