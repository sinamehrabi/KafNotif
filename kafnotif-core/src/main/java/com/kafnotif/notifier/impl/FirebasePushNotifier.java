package com.kafnotif.notifier.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.kafnotif.model.Event;
import com.kafnotif.model.PushNotification;
import com.kafnotif.notifier.PushNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging implementation of PushNotifier
 */
public class FirebasePushNotifier implements PushNotifier {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebasePushNotifier.class);
    
    private final FirebaseMessaging firebaseMessaging;
    
    /**
     * Constructor with service account JSON file path
     */
    public FirebasePushNotifier(String serviceAccountPath) throws IOException {
        this.firebaseMessaging = initializeFirebase(serviceAccountPath);
    }
    
    /**
     * Constructor with service account JSON as InputStream
     */
    public FirebasePushNotifier(InputStream serviceAccountStream) throws IOException {
        this.firebaseMessaging = initializeFirebase(serviceAccountStream);
    }
    
    /**
     * Constructor with default FirebaseApp (must be already initialized)
     */
    public FirebasePushNotifier() {
        this.firebaseMessaging = FirebaseMessaging.getInstance();
    }
    
    /**
     * Constructor with specific FirebaseApp
     */
    public FirebasePushNotifier(FirebaseApp firebaseApp) {
        this.firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
    }
    
    /**
     * Initialize Firebase with service account file
     */
    private FirebaseMessaging initializeFirebase(String serviceAccountPath) throws IOException {
        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
            return initializeFirebase(serviceAccount);
        }
    }
    
    /**
     * Initialize Firebase with service account InputStream
     */
    private FirebaseMessaging initializeFirebase(InputStream serviceAccountStream) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
            .build();
        
        // Use a unique app name to avoid conflicts
        String appName = "kafnotif-" + System.currentTimeMillis();
        FirebaseApp app = FirebaseApp.initializeApp(options, appName);
        
        return FirebaseMessaging.getInstance(app);
    }
    
    @Override
    public boolean sendPushNotification(PushNotification notification) {
        try {
            Message.Builder messageBuilder = Message.builder()
                .setToken(notification.getDeviceToken())
                .setNotification(Notification.builder()
                    .setTitle(notification.getTitle())
                    .setBody(notification.getBody())
                    .setImage(notification.getIcon()) // Using icon as image
                    .build());
            
            // Add custom data if present
            if (notification.getData() != null && !notification.getData().isEmpty()) {
                messageBuilder.putAllData(notification.getData());
            }
            
            // Configure platform-specific options based on platform
            if (notification.getPlatform() == PushNotification.PushPlatform.ANDROID) {
                messageBuilder.setAndroidConfig(buildAndroidConfig(notification));
            }
            
            if (notification.getPlatform() == PushNotification.PushPlatform.IOS) {
                messageBuilder.setApnsConfig(buildApnsConfig(notification));
            }
            
            Message message = messageBuilder.build();
            String response = firebaseMessaging.send(message);
            
            logger.info("Push notification sent successfully. Response: {}", response);
            return true;
            
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send push notification to device: {}", notification.getDeviceToken(), e);
            return false;
        }
    }
    
    @Override
    public boolean sendSimplePush(String deviceToken, String title, String body) {
        PushNotification notification = new PushNotification();
        notification.setDeviceToken(deviceToken);
        notification.setTitle(title);
        notification.setBody(body);
        return sendPushNotification(notification);
    }
    
    @Override
    public boolean sendPushToMultipleDevices(String[] deviceTokens, String title, String body) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .addAllTokens(Arrays.asList(deviceTokens))
                .build();
            
            BatchResponse response = firebaseMessaging.sendMulticast(message);
            
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        logger.error("Failed to send push notification to device {}: {}", 
                                   deviceTokens[i], responses.get(i).getException());
                    }
                }
            }
            
            logger.info("Push notifications sent. Success: {}, Failure: {}", 
                       response.getSuccessCount(), response.getFailureCount());
            
            return response.getFailureCount() == 0;
            
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send push notifications to multiple devices", e);
            return false;
        }
    }
    
    @Override
    public boolean sendPushWithData(PushNotification notification) {
        return sendPushNotification(notification);
    }
    
    @Override
    public void send(Event event) {
        if (event instanceof PushNotification) {
            sendPushNotification((PushNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be a PushNotification");
        }
    }
    
    private AndroidConfig buildAndroidConfig(PushNotification notification) {
        AndroidConfig.Builder builder = AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH);
        
        if (notification.getCollapseKey() != null) {
            builder.setCollapseKey(notification.getCollapseKey());
        }
        
        if (notification.getTtl() != null) {
            builder.setTtl(notification.getTtl() * 1000L); // Convert to milliseconds
        }
        
        return builder.build();
    }
    
    private ApnsConfig buildApnsConfig(PushNotification notification) {
        ApnsConfig.Builder builder = ApnsConfig.builder();
        
        // Set APNS priority to high
        builder.putHeader("apns-priority", "10");
        
        if (notification.getTtl() != null) {
            long expiration = System.currentTimeMillis() / 1000 + notification.getTtl();
            builder.putHeader("apns-expiration", String.valueOf(expiration));
        }
        
        return builder.build();
    }
}
