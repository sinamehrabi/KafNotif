package com.kafnotif.model;

/**
 * Priority levels for notifications
 */
public enum NotificationPriority {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4),
    CRITICAL(5);

    private final int level;

    NotificationPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static NotificationPriority fromLevel(int level) {
        for (NotificationPriority priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority level: " + level);
    }
}
