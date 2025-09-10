# 🚀 KafNotif Simple Example

A clean, minimal Spring Boot application demonstrating KafNotif usage with real-world patterns.

## 🎯 What's Included

- **Controller** - REST endpoints for sending notifications
- **Listeners** - Kafka consumers with `@KafNotifListener` annotations
- **Configuration** - Production-ready YAML configuration
- **Multi-Channel Slack** - Example of channel-based routing

## 🏃‍♂️ Quick Start

### 1. Start Dependencies
```bash
# Start Kafka (using Docker)
docker run -p 9092:9092 apache/kafka:2.8.0

# Start MailCatcher for email testing
docker run -d -p 1080:1080 -p 1025:1025 schickling/mailcatcher
```

### 2. Run Application
```bash
mvn spring-boot:run
```

### 3. Test Endpoints

**Send Email:**
```bash
curl -X POST http://localhost:8080/api/notifications/email \
  -H "Content-Type: application/json" \
  -d '{"to": "test@example.com", "subject": "Hello", "body": "Test message"}'
```

**Send Push Notification:**
```bash
curl -X POST http://localhost:8080/api/notifications/push \
  -H "Content-Type: application/json" \
  -d '{"deviceToken": "test-token", "title": "Hello", "body": "Test push", "platform": "ANDROID"}'
```

**Send Slack Message:**
```bash
curl -X POST http://localhost:8080/api/notifications/slack \
  -H "Content-Type: application/json" \
  -d '{"channel": "alerts", "text": "Test alert message"}'
```

**Health Check:**
```bash
curl http://localhost:8080/api/notifications/health
```

## 📧 Email Testing

- View emails at: http://localhost:1080 (MailCatcher)
- No SMTP credentials needed for development

## 🔔 Push Notification Setup

1. **Get Firebase Service Account:**
   - Go to Firebase Console → Project Settings → Service Accounts
   - Generate new private key (downloads JSON file)
   - Place it in your project and update path in `application.yml`

2. **Update Configuration:**
```yaml
kafnotif:
  providers:
    firebase:
      enabled: true
      service-account-path: /path/to/your/service-account.json
```

3. **Test with Real Device Token:**
```bash
curl -X POST http://localhost:8080/api/notifications/push \
  -H "Content-Type: application/json" \
  -d '{"deviceToken": "REAL_FCM_TOKEN", "title": "Hello!", "body": "From KafNotif", "platform": "ANDROID"}'
```

## 💬 Slack Configuration

Update `application.yml` with your webhook URLs:

```yaml
kafnotif:
  providers:
    slack:
      enabled: true
      channels:
        general:
          webhook-url: https://hooks.slack.com/services/YOUR/GENERAL/WEBHOOK
        alerts:
          webhook-url: https://hooks.slack.com/services/YOUR/ALERTS/WEBHOOK
```

## 🎯 Key Features Demonstrated

- **✅ Auto-processing** - Email & Push automatically sent via concrete implementations
- **🎛️ Custom afterSend hooks** - Full control over post-processing
- **📧 Email via JavaMail** - Configured for MailCatcher development setup
- **🔔 Push via Firebase FCM** - Real push notifications to mobile devices
- **💬 Multi-channel Slack** - Different webhooks per channel
- **🧵 Virtual Threads** - Java 21+ high-performance concurrency
- **📋 Manual ACK** - Precise message acknowledgment control
- **⚙️ Configuration** - Production-ready setup examples

## 📝 Code Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java           # Spring Boot main class
├── NotificationController.java    # REST endpoints
├── listeners/
│   ├── EmailNotificationListener.java  # Email handling with afterSend hook
│   ├── PushNotificationListener.java   # Push notification handling
│   └── SlackNotificationListener.java  # Slack handling
├── NotificationConfiguration.java # Bean configuration

src/main/resources/
└── application.yml                # KafNotif configuration
```

This example shows the **simplest possible** way to get started with KafNotif! 🎯
