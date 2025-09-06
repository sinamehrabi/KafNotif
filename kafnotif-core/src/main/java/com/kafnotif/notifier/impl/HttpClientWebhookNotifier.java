package com.kafnotif.notifier.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafnotif.util.JsonUtils;
import com.kafnotif.model.Event;
import com.kafnotif.model.WebhookNotification;
import com.kafnotif.notifier.WebhookNotifier;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Apache HttpClient implementation of WebhookNotifier
 */
public class HttpClientWebhookNotifier implements WebhookNotifier {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpClientWebhookNotifier.class);
    
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor with default HTTP client
     */
    public HttpClientWebhookNotifier() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = JsonUtils.createObjectMapper();
    }
    
    /**
     * Constructor with custom HTTP client
     */
    public HttpClientWebhookNotifier(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = JsonUtils.createObjectMapper();
    }
    
    @Override
    public boolean sendWebhook(WebhookNotification notification) {
        return sendCustomWebhook(
            notification.getUrl(),
            notification.getMethod(),
            notification.getWebhookPayload(),
            notification.getHeaders()
        );
    }
    
    @Override
    public boolean sendSimpleWebhook(String url, Map<String, Object> payload) {
        return sendWebhookWithHeaders(url, payload, null);
    }
    
    @Override
    public boolean sendWebhookWithHeaders(String url, Map<String, Object> payload, Map<String, String> headers) {
        return sendCustomWebhook(url, "POST", payload, headers);
    }
    
    @Override
    public boolean sendCustomWebhook(String url, String method, Map<String, Object> payload, Map<String, String> headers) {
        try {
            HttpUriRequestBase request = createHttpRequest(method, url);
            
            // Add headers
            if (headers != null) {
                headers.forEach(request::setHeader);
            }
            
            // Add body for methods that support it
            if (payload != null && !method.equalsIgnoreCase("GET")) {
                String jsonBody = objectMapper.writeValueAsString(payload);
                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            }
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                
                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Webhook sent successfully to: {}. Status: {}", url, statusCode);
                    return true;
                } else {
                    logger.error("Webhook failed. URL: {}, Status: {}", url, statusCode);
                    return false;
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to send webhook to: {}", url, e);
            return false;
        }
    }
    
    @Override
    public void send(Event event) {
        if (event instanceof WebhookNotification) {
            sendWebhook((WebhookNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be a WebhookNotification");
        }
    }
    
    private HttpUriRequestBase createHttpRequest(String method, String url) {
        return switch (method.toUpperCase()) {
            case "GET" -> new HttpGet(url);
            case "POST" -> new HttpPost(url);
            case "PUT" -> new HttpPut(url);
            case "DELETE" -> new HttpDelete(url);
            case "PATCH" -> new HttpPatch(url);
            case "HEAD" -> new HttpHead(url);
            case "OPTIONS" -> new HttpOptions(url);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }
    
    /**
     * Close the HTTP client
     */
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
