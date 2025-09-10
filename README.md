# 🚀 KafNotif - Zero-Code Kafka Notification System

**The most powerful, production-ready notification system with automatic sending and complete lifecycle control**

[![JitPack](https://jitpack.io/v/sinamehrabi/KafNotif.svg)](https://jitpack.io/#sinamehrabi/KafNotif)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## ✨ What is KafNotif?

KafNotif revolutionizes notification systems by eliminating complex infrastructure code. Simply add `@KafNotifListener` to your methods and KafNotif automatically handles:

- 📧 **Email** sending via JavaMail (Gmail, Outlook, MailCatcher)
- 📱 **Push notifications** via Firebase Cloud Messaging (FCM)  
- 📲 **SMS** via Twilio
- 💬 **Slack & Discord** webhooks
- 🌐 **HTTP webhooks** for any API
- 🎯 **Custom afterSend hooks** for complete lifecycle control

## 🎯 Key Features

### 🤖 **Zero-Code Automation**
```java
@KafNotifListener(NotificationType.EMAIL)
public void handleEmail(EmailNotification email) {
    // KafNotif automatically sends the email!
    System.out.println("📧 Processing: " + email.getSubject());
}
```

### 🎛️ **Complete Lifecycle Control with AfterSend Hooks**
```java
@KafNotifListener(
    value = NotificationType.EMAIL,
    ackMode = AckMode.MANUAL,
    afterSend = "emailAfterHook"  // 🎯 Override default behavior
)
public void emailListener(EmailNotification email) {
    System.out.println("🔔 Email will be sent: " + email.getSubject());
}

// Your custom afterSend hook completely overrides default behavior
public void emailAfterHook(EmailNotification email, boolean success, Exception error, AckControl ackControl) {
    if (success) {
        // Custom success logic
        saveToDatabase(email.getId(), "SENT");
        sendAnalytics("email_sent", email.getRecipient());
        ackControl.acknowledge(); // YOU control acknowledgment
    } else {
        // Custom error handling
        if (isRetryableError(error)) {
            // Don't acknowledge - let it retry
            logger.warn("Retryable error: {}", error.getMessage());
        } else {
            // Acknowledge to skip permanently failed messages
            ackControl.acknowledge();
            logPermanentFailure(email, error);
        }
    }
}
```

### ⚡ **High Performance**
- **Virtual Threads** support (Java 21+)
- **Configurable concurrency** per notification type
- **Auto/Manual acknowledgment** modes
- **Thread-safe** acknowledgment with Spring Kafka patterns

### 🏗️ **Production Ready**
- **Auto-topic creation** when publishing
- **Dead Letter Queue** for failed messages
- **Retry mechanisms** with configurable strategies
- **Comprehensive monitoring** and logging

## 🚀 Quick Start

### 1. Add Dependency
```xml
<dependency>
    <groupId>com.github.sinamehrabi</groupId>
    <artifactId>KafNotif</artifactId>
    <version>v0.1.6</version>
</dependency>
```

### 2. Configure (application.yml)
```yaml
kafnotif:
  bootstrap-servers: localhost:9092
  providers:
    email:
      enabled: true
      smtp-host: localhost  # MailCatcher for development
      smtp-port: 1025
      from-email: noreply@yourapp.com
      # For production: add username/password
```

### 3. Create Notification Listeners
```java
@Component
public class NotificationListeners {
    
    // 🎯 Basic listener with auto-sending
    @KafNotifListener(NotificationType.EMAIL)
    public void handleEmail(EmailNotification email) {
        System.out.println("📧 Processing: " + email.getSubject());
        // KafNotif automatically sends the email!
    }
    
    // 🎛️ Advanced listener with complete control
    @KafNotifListener(
        value = NotificationType.EMAIL,
        concurrency = 5,
        ackMode = AckMode.MANUAL,
        threadingMode = ThreadingMode.VIRTUAL_THREADS,
        afterSend = "customAfterHook"
    )
    public void advancedEmailHandler(EmailNotification email) {
        // Add pre-processing logic
        enrichEmailContent(email);
    }
    
    // 🎯 Custom afterSend hook with complete override
    public void customAfterHook(EmailNotification email, boolean success, Exception error, AckControl ackControl) {
        if (success) {
            updateDatabase(email.getId(), "DELIVERED");
            trackAnalytics("email_success", email.getRecipient());
            ackControl.acknowledge();
        } else {
            handleFailure(email, error);
            if (shouldRetry(error)) {
                // Don't acknowledge - let it retry
            } else {
                ackControl.acknowledge(); // Skip permanently failed
            }
        }
    }
}
```

### 4. Publish Notifications
```java
@RestController
public class NotificationController {
    
    @Autowired
    private NotificationPublisher publisher;
    
    @PostMapping("/send-email")
    public String sendEmail() {
        publisher.publishNotification(EmailNotification.builder()
            .recipient("user@example.com")
            .subject("Welcome to our platform!")
            .body("Thanks for joining us!")
            .build());
        return "📧 Email queued for sending!";
    }
}
```

## 📚 **Examples**

Ready-to-run examples are available in the [`kafnotif-examples/`](kafnotif-examples/) directory:

- **🎯 [Simple Example](kafnotif-examples/example/)** - Clean Spring Boot app with REST API and listeners
- **🔧 Manual Integration** - Custom setup for advanced use cases

## 🎛️ AfterSend Hooks - Complete Guide

### 🌟 **Why Use AfterSend Hooks?**

AfterSend hooks give you **complete control** over what happens after notification sending:
- 📊 **Analytics tracking** for successful/failed sends
- 🗃️ **Database updates** for notification status
- 🔄 **Custom retry logic** for specific error types
- 🎯 **Manual acknowledgment control** for exact-once processing
- 🚨 **Custom error handling** and alerting

### 🎯 **How AfterSend Override Works**

When you specify `afterSend = "methodName"`:
- ✅ **Your method completely overrides** default auto-acknowledge behavior
- ✅ **No automatic acknowledgment** happens - YOU are in control
- ✅ **Full access** to success/failure status and error details
- ✅ **Manual acknowledgment control** for retry behavior

### 📝 **Supported Method Signatures**

```java
// 🎯 Full control signature (recommended)
public void afterHook(EmailNotification email, boolean success, Exception error, AckControl ackControl) {
    // Full control with acknowledgment management
}

// 🎯 Alternative with Throwable
public void afterHook(EmailNotification email, boolean success, Throwable error, AckControl ackControl) {
    // Same as above but with Throwable instead of Exception
}

// 🎯 Simplified signature
public void afterHook(EmailNotification email, boolean success) {
    // Basic success/failure handling without acknowledgment control
}
```

### 🎪 **Real-World Examples**

#### 📊 **Analytics Tracking**
```java
@KafNotifListener(
    value = NotificationType.EMAIL,
    afterSend = "trackEmailMetrics"
)
public void emailHandler(EmailNotification email) {
    enrichWithUserData(email);
}

public void trackEmailMetrics(EmailNotification email, boolean success, Exception error, AckControl ackControl) {
    if (success) {
        analytics.track("email_delivered", Map.of(
            "recipient", email.getRecipient(),
            "campaign", email.getMetadata().get("campaign"),
            "delivered_at", Instant.now()
        ));
        ackControl.acknowledge();
    } else {
        analytics.track("email_failed", Map.of(
            "recipient", email.getRecipient(),
            "error_type", error.getClass().getSimpleName(),
            "error_message", error.getMessage()
        ));
        
        // Custom retry logic
        if (isTemporaryError(error)) {
            // Don't acknowledge - let Kafka retry
            logger.info("Temporary error, will retry: {}", error.getMessage());
        } else {
            // Permanent error - acknowledge to skip
            ackControl.acknowledge();
            logger.error("Permanent error, skipping: {}", error.getMessage());
        }
    }
}
```

#### 🗃️ **Database Integration**
```java
@KafNotifListener(
    value = NotificationType.EMAIL,
    afterSend = "updateDatabase"
)
public void emailHandler(EmailNotification email) {
    validateEmailContent(email);
}

public void updateDatabase(EmailNotification email, boolean success, Exception error, AckControl ackControl) {
    try {
        NotificationLog log = NotificationLog.builder()
            .notificationId(email.getId())
            .recipient(email.getRecipient())
            .status(success ? "DELIVERED" : "FAILED")
            .errorMessage(error != null ? error.getMessage() : null)
            .processedAt(Instant.now())
            .build();
            
        notificationRepository.save(log);
        
        if (success) {
            // Update user engagement metrics
            userService.recordEmailEngagement(email.getRecipient());
        }
        
        ackControl.acknowledge();
    } catch (Exception dbError) {
        logger.error("Database update failed", dbError);
        // Still acknowledge to prevent reprocessing
        ackControl.acknowledge();
    }
}
```

#### 🔄 **Multi-Step Workflow**
```java
@KafNotifListener(
    value = NotificationType.EMAIL,
    afterSend = "triggerWorkflow"
)
public void emailHandler(EmailNotification email) {
    addTrackingPixel(email);
}

public void triggerWorkflow(EmailNotification email, boolean success, Exception error, AckControl ackControl) {
    if (success) {
        // Trigger next step in workflow
        switch (email.getMetadata().get("workflow_type")) {
            case "welcome":
                scheduleFollowUpEmail(email.getRecipient(), Duration.ofDays(3));
                break;
            case "order_confirmation":
                updateOrderStatus(email.getMetadata().get("order_id"), "EMAIL_SENT");
                break;
            case "password_reset":
                logSecurityEvent("password_reset_email_sent", email.getRecipient());
                break;
        }
        ackControl.acknowledge();
    } else {
        // Handle workflow failure
        workflowService.markStepFailed(email.getId(), error.getMessage());
        
        // Retry logic based on workflow criticality
        boolean isCritical = "order_confirmation".equals(email.getMetadata().get("workflow_type"));
        if (isCritical && isRetryableError(error)) {
            // Don't acknowledge - let it retry
            logger.warn("Critical email failed, will retry: {}", error.getMessage());
        } else {
            ackControl.acknowledge();
        }
    }
}
```

### ⚙️ **Configuration Options**

```java
@KafNotifListener(
    value = NotificationType.EMAIL,           // Notification type
    concurrency = 5,                          // Concurrent consumers
    ackMode = AckMode.MANUAL,                 // Manual acknowledgment control
    threadingMode = ThreadingMode.VIRTUAL_THREADS, // Java 21+ Virtual Threads
    maxRetries = 3,                           // Retry attempts
    groupId = "email-processors",             // Custom consumer group
    afterSend = "customAfterHook"             // Your custom hook
)
```

## 💬 **Multi-Channel Slack Usage Examples**

```java
// Send to specific channels with automatic webhook routing
SlackNotification alertMsg = new SlackNotification("alerts", "🚨 Database connection lost!");
// Uses alerts webhook automatically, applies "🚨 Alert System" username
publisher.publishNotification(alertMsg);

SlackNotification reportMsg = new SlackNotification("reports", "📊 Daily analytics ready");
// Uses reports webhook automatically, applies "📊 Report Bot" username
publisher.publishNotification(reportMsg);

SlackNotification devMsg = new SlackNotification("dev-team", "🚀 Deployment completed v2.1.0");
devMsg.setUsername("🚀 Deploy Bot"); // Override default username
devMsg.setIconEmoji(":rocket:");
publisher.publishNotification(devMsg);

// Send to default channel (fallback)
SlackNotification generalMsg = new SlackNotification("general", "📢 System maintenance tonight");
publisher.publishNotification(generalMsg);

// Advanced usage with blocks and attachments
SlackNotification richMsg = new SlackNotification("alerts", "Critical Issue Detected");
richMsg.setUsername("🔥 Critical Alert");
richMsg.setIconEmoji(":fire:");

// Add Slack blocks for rich formatting
List<Map<String, Object>> blocks = new ArrayList<>();
Map<String, Object> block = new HashMap<>();
block.put("type", "section");
Map<String, Object> text = new HashMap<>();
text.put("type", "mrkdwn");
text.put("text", "*Database Error*\n:warning: Connection timeout after 30s");
block.put("text", text);
blocks.add(block);
richMsg.setBlocks(blocks);

publisher.publishNotification(richMsg);
```

### 🎯 **Channel-Based Bot Personas**
- **alerts** → "🚨 Alert System" - For urgent notifications
- **reports** → "📊 Report Bot" - For analytics and metrics  
- **dev-team** → "🔧 Dev Bot" - For deployment and development updates
- **general** → "📢 KafNotif Bot" - For general announcements

### ✅ **Backward Compatibility**
```yaml
# Old single-webhook configuration still works
slack:
  enabled: true
  webhook-url: https://hooks.slack.com/services/T.../B.../single-webhook
```

## 📦 Supported Notification Types

### 📧 **Email (JavaMail)**
```yaml
kafnotif:
  providers:
    email:
      enabled: true
      smtp-host: smtp.gmail.com
      smtp-port: 587
      username: your-email@gmail.com
      password: your-app-password
      from-email: noreply@yourapp.com
```

### 📱 **Push Notifications (Firebase FCM)**
```yaml
kafnotif:
  providers:
    push:
      enabled: true
      firebase-credentials-path: /path/to/service-account.json
```

### 📲 **SMS (Twilio)**
```yaml
kafnotif:
  providers:
    sms:
      enabled: true
      account-sid: your-twilio-sid
      auth-token: your-twilio-token
      from-number: +1234567890
```

### 💬 **Slack (Multi-Channel Support)**
```yaml
kafnotif:
  providers:
    slack:
      enabled: true
      default-channel: general
      channels:
        general:
          webhook-url: https://hooks.slack.com/services/T.../B.../general-webhook
          default-username: "📢 KafNotif Bot"
        alerts:
          webhook-url: https://hooks.slack.com/services/T.../B.../alerts-webhook
          default-username: "🚨 Alert System"
        reports:
          webhook-url: https://hooks.slack.com/services/T.../B.../reports-webhook
          default-username: "📊 Report Bot"
        dev-team:
          webhook-url: https://hooks.slack.com/services/T.../B.../dev-webhook
          default-username: "🔧 Dev Bot"
      
      # Backward compatibility - single webhook
      # webhook-url: https://hooks.slack.com/services/...
```

### 🎮 **Discord**
```yaml
kafnotif:
  providers:
    discord:
      enabled: true
      webhook-url: https://discord.com/api/webhooks/...
```

## 🛠️ Development Setup

### 🐳 **MailCatcher for Email Testing**
```bash
# Start MailCatcher
docker run -d -p 1080:1080 -p 1025:1025 schickling/mailcatcher

# Configure KafNotif for MailCatcher
kafnotif:
  providers:
    email:
      enabled: true
      smtp-host: localhost
      smtp-port: 1025
      from-email: test@kafnotif.com
      # No username/password needed for MailCatcher
```

View emails at: http://localhost:1080

### 🔧 **IDE Integration**
KafNotif includes `@EventListener` annotation for perfect IntelliJ IDEA integration:
- ✅ No "unused method" warnings
- ✅ Full Spring framework recognition
- ✅ Perfect developer experience

## 🚀 Production Deployment

### 🎯 **Best Practices**
- Use **Manual ACK mode** with afterSend hooks for critical notifications
- Configure **appropriate concurrency** based on your throughput needs
- Set up **monitoring** for Dead Letter Queue topics
- Use **Virtual Threads** for high concurrency (Java 21+)
- Implement **comprehensive error handling** in afterSend hooks

### 📊 **Monitoring**
```java
// Monitor processing metrics in your afterSend hooks
public void monitoringHook(EmailNotification email, boolean success, Exception error, AckControl ackControl) {
    meterRegistry.counter("kafnotif.email.processed", 
        "status", success ? "success" : "failure",
        "recipient_domain", extractDomain(email.getRecipient())
    ).increment();
    
    if (success) {
        ackControl.acknowledge();
    } else {
        // Handle based on error type
        handleError(email, error, ackControl);
    }
}
```

## 🤝 Contributing

We welcome contributions! Please see our contributing guidelines and feel free to:
- 🐛 Report issues
- 💡 Submit feature requests
- 🔧 Create pull requests
- 📚 Improve documentation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**🎉 Ready to revolutionize your notification system? Get started with KafNotif today!**

[![JitPack](https://jitpack.io/v/sinamehrabi/KafNotif.svg)](https://jitpack.io/#sinamehrabi/KafNotif)