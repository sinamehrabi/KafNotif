package com.kafnotif.model;

/**
 * Enumeration of supported notification types
 */
public enum NotificationType {
    EMAIL("email"),
    SMS("sms"), 
    PUSH("push"),
    SLACK("slack"),
    DISCORD("discord"),
    WEBHOOK("webhook");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NotificationType fromString(String value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
