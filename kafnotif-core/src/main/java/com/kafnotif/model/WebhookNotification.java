package com.kafnotif.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Generic webhook notification implementation
 */
public class WebhookNotification extends NotificationEvent {
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("method")
    private String method = "POST";
    
    @JsonProperty("headers")
    private Map<String, String> headers;
    
    @JsonProperty("payload")
    private Map<String, Object> payload;
    
    @JsonProperty("contentType")
    private String contentType = "application/json";
    
    @JsonProperty("timeout")
    private Integer timeout = 30; // seconds

    public WebhookNotification() {
        super();
    }

    public WebhookNotification(String url, Map<String, Object> payload) {
        super(NotificationType.WEBHOOK, url);
        this.url = url;
        this.payload = payload;
    }

    public WebhookNotification(String url, String method, Map<String, Object> payload) {
        this(url, payload);
        this.method = method;
    }

    @Override
    public boolean isValid() {
        return url != null && !url.trim().isEmpty() &&
               method != null && !method.trim().isEmpty() &&
               payload != null && !payload.isEmpty() &&
               isValidUrl(url);
    }

    @Override
    public String getContent() {
        return payload != null ? payload.toString() : "";
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getWebhookPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
