package com.kafnotif.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Push notification specific implementation
 */
public class PushNotification extends NotificationEvent {
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("body")
    private String body;
    
    @JsonProperty("icon")
    private String icon;
    
    @JsonProperty("badge")
    private Integer badge;
    
    @JsonProperty("sound")
    private String sound;
    
    @JsonProperty("clickAction")
    private String clickAction;
    
    @JsonProperty("data")
    private Map<String, String> data;
    
    @JsonProperty("deviceToken")
    private String deviceToken;
    
    @JsonProperty("platform")
    private PushPlatform platform; // iOS, Android, Web
    
    @JsonProperty("collapseKey")
    private String collapseKey;
    
    @JsonProperty("ttl")
    private Integer ttl; // Time to live in seconds

    public PushNotification() {
        super();
    }

    public PushNotification(String deviceToken, String title, String body) {
        super(NotificationType.PUSH, deviceToken);
        this.deviceToken = deviceToken;
        this.title = title;
        this.body = body;
    }

    public PushNotification(String deviceToken, String title, String body, PushPlatform platform) {
        this(deviceToken, title, body);
        this.platform = platform;
    }

    @Override
    public boolean isValid() {
        return deviceToken != null && !deviceToken.trim().isEmpty() &&
               title != null && !title.trim().isEmpty() &&
               body != null && !body.trim().isEmpty();
    }

    @Override
    public String getContent() {
        return body;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getBadge() {
        return badge;
    }

    public void setBadge(Integer badge) {
        this.badge = badge;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getClickAction() {
        return clickAction;
    }

    public void setClickAction(String clickAction) {
        this.clickAction = clickAction;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public PushPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(PushPlatform platform) {
        this.platform = platform;
    }

    public String getCollapseKey() {
        return collapseKey;
    }

    public void setCollapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public enum PushPlatform {
        IOS, ANDROID, WEB
    }
}
