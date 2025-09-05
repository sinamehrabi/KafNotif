# 🤖 KafNotif FULLY AUTOMATIC Processing Example

This example demonstrates **ZERO-CODE NOTIFICATION SENDING** with KafNotif. 

## ✨ What You Get

- **🎯 Per-Type Configuration**: Different concurrency, ACK modes, and threading per notification type
- **📝 Simple Annotations**: Just add `@KafNotifListener` - NO implementation needed!
- **🤖 AUTOMATIC SENDING**: KafNotif uses JavaMail, Firebase, Twilio, Slack, Discord automatically
- **🔄 Automatic Consumer Management**: KafNotif handles all Kafka consumer lifecycle
- **⚡ Optimized Settings**: Different configurations optimized for each notification type
- **🛠️ Easy Testing**: REST API to publish test notifications
- **🎭 Optional Hooks**: Add custom logic before/after automatic sending (optional!)

## 🏗️ Architecture

```
📧 Email    → @KafNotifListener(concurrency=10) → [Optional Hook] → JavaMail AUTOMATIC ✅
📱 SMS      → @KafNotifListener(concurrency=5)  → [Optional Hook] → Twilio AUTOMATIC ✅
🔔 Push     → @KafNotifListener(concurrency=2)  → [Optional Hook] → Firebase AUTOMATIC ✅
💬 Slack    → @KafNotifListener(concurrency=3)  → [No Hook Needed] → Slack Webhook AUTOMATIC ✅
🎮 Discord  → @KafNotifListener(concurrency=3)  → [No Hook Needed] → Discord Webhook AUTOMATIC ✅
🌐 Webhook  → @KafNotifListener(concurrency=7)  → [Optional Hook] → HTTP Client AUTOMATIC ✅
```

**🎯 The Magic**: You just add `@KafNotifListener` and KafNotif handles ALL the sending!
- **NO** JavaMail code needed - automatic!
- **NO** Firebase/FCM code needed - automatic!  
- **NO** Twilio SDK code needed - automatic!
- **NO** Slack/Discord webhook code needed - automatic!
- **NO** HTTP client code needed - automatic!

## 👨‍💻 Code Examples

### Before KafNotif (Traditional Way)
```java
@KafkaListener(topics = "email-notifications")  
public void processEmail(EmailNotification email) {
    // YOU have to implement all this:
    Session session = Session.getDefaultInstance(smtpProps, authenticator);
    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress(fromEmail));
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(email.getRecipient()));
    message.setSubject(email.getSubject());
    message.setText(email.getBody());
    Transport.send(message);
    // Plus error handling, retries, etc...
}
```

### After KafNotif (Zero Code!)
```java
@KafNotifListener(value = NotificationType.EMAIL, concurrency = 10)
public void emailProcessor() {
    // THAT'S IT! KafNotif automatically sends via JavaMail!
    // Optional: add your custom logic here (validation, logging, etc.)
}

// Or even simpler - no method needed at all!
@KafNotifListener(value = NotificationType.SMS, concurrency = 5)
public void smsProcessor() {} // Twilio SMS sent automatically!

@KafNotifListener(value = NotificationType.PUSH, concurrency = 2)  
public void pushProcessor() {} // Firebase FCM sent automatically!
```

**🎉 Result**: KafNotif automatically handles JavaMail, Firebase, Twilio, Slack, Discord, HTTP calls!

## 🚀 Running the Example

### 1. Start Kafka
```bash
# Using Docker
docker run -p 9092:9092 apache/kafka:2.8.0

# Or using local Kafka
bin/kafka-server-start.sh config/server.properties
```

### 2. Start the Application
```bash
cd kafnotif-examples/automatic-processing-example
mvn spring-boot:run
```

### 3. The application will:
- ✅ Auto-configure KafNotif with global settings
- ✅ Discover all `@KafNotifListener` annotated methods
- ✅ Create optimized consumers for each notification type
- ✅ Start all consumers automatically
- ✅ Expose REST endpoints for testing

## 📡 Testing the API

### Send Test Notifications

**📧 Email:**
```bash
curl -X POST http://localhost:8083/api/notifications/email \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Hello from KafNotif!",
    "body": "This is a test email"
  }'
```

**📱 SMS:**
```bash
curl -X POST http://localhost:8083/api/notifications/sms \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "+1234567890",
    "message": "Hello from KafNotif SMS!",
    "countryCode": "+1"
  }'
```

**🔔 Push:**
```bash
curl -X POST http://localhost:8083/api/notifications/push \
  -H "Content-Type: application/json" \
  -d '{
    "deviceToken": "device-token-123",
    "title": "KafNotif Push",
    "body": "Test push notification",
    "platform": "ANDROID"
  }'
```

**💬 Slack:**
```bash
curl -X POST http://localhost:8083/api/notifications/slack \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "#general",
    "text": "Hello from KafNotif!",
    "username": "KafNotif Bot"
  }'
```

**🎮 Discord:**
```bash
curl -X POST http://localhost:8083/api/notifications/discord \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hello from KafNotif Discord!",
    "username": "KafNotif Bot"
  }'
```

**🌐 Webhook:**
```bash
curl -X POST http://localhost:8083/api/notifications/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://httpbin.org/post",
    "method": "POST",
    "payload": {
      "message": "Hello from KafNotif webhook!",
      "timestamp": "2023-12-01T10:00:00Z"
    }
  }'
```

**🔀 Batch (Multiple):**
```bash
curl -X POST http://localhost:8083/api/notifications/batch
```

## 📊 What You'll See

When you send notifications, you'll see logs like:
```
📧 Processing EMAIL: Hello from KafNotif! -> user@example.com
   Body: This is a test email
   Priority: NORMAL
✅ Email processed successfully

📱 Processing SMS: Hello from KafNotif SMS! -> +1234567890
   Country Code: +1
✅ SMS processed successfully

🔔 Processing PUSH: KafNotif Push -> device-token-123
   Body: Test push notification
   Platform: ANDROID
✅ Push notification processed successfully
```

## 🎯 Key Features Demonstrated

### Different Configurations Per Type
- **Email**: High concurrency (10), MANUAL_IMMEDIATE ACK, Virtual threads
- **SMS**: Medium concurrency (5), MANUAL ACK, Virtual threads  
- **Push**: Low concurrency (2), MANUAL_IMMEDIATE ACK, Platform threads (FCM optimized)
- **Slack**: Medium concurrency (3), AUTO ACK (fire-and-forget)
- **Discord**: Medium concurrency (3), MANUAL ACK
- **Webhook**: High concurrency (7), MANUAL ACK

### Multi-Type Listeners
```java
@KafNotifListener(
    types = {NotificationType.EMAIL, NotificationType.SMS},
    concurrency = 15,
    groupId = "batch-processor"
)
public void processBatch(NotificationEvent notification) {
    // Handle multiple types in one method
}
```

### Simple Configuration
```yaml
kafnotif:
  bootstrap-servers: localhost:9092
  group-id: automatic-app
  topic-prefix: notifications
  threading-mode: VIRTUAL_THREADS
  concurrency: 5
  ack-mode: MANUAL
```

## 🔧 Customization

### Override Global Settings
```java
@KafNotifListener(
    value = NotificationType.EMAIL,
    concurrency = 20,                    // Override global concurrency
    ackMode = AckMode.AUTO,              // Override global ACK mode
    threadingMode = ThreadingMode.PLATFORM_THREADS, // Override threading
    groupId = "custom-email-processor",  // Custom consumer group
    bootstrapServers = "custom-kafka:9092", // Override Kafka servers
    topicPrefix = "custom-notifications"    // Override topic prefix
)
```

### Multiple Consumer Groups
```java
@KafNotifListener(value = NotificationType.EMAIL, groupId = "email-group-1")
public void processEmailGroup1(EmailNotification email) { }

@KafNotifListener(value = NotificationType.EMAIL, groupId = "email-group-2") 
public void processEmailGroup2(EmailNotification email) { }
```

## 🎉 Benefits

✅ **Zero Complex Setup**: Just add annotations  
✅ **Per-Type Optimization**: Different settings for different notification types  
✅ **Automatic Lifecycle**: KafNotif manages all consumers  
✅ **Spring Integration**: Works seamlessly with Spring Boot  
✅ **Production Ready**: Built-in retries, DLQ, error handling  
✅ **Highly Configurable**: Override any setting per listener  
✅ **Developer Friendly**: Simple, annotation-driven approach  

This is exactly what you wanted - simple annotations that configure different consumers per notification type! 🚀