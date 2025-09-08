# KafNotif

🤖 **ZERO-CODE** Kafka-based notification system for Java applications. Just add `@KafNotifListener` and KafNotif **automatically sends** notifications via JavaMail, Firebase, Twilio, Slack, Discord, and HTTP webhooks!

## ✨ Key Features

- 🤖 **AUTOMATIC SENDING**: Zero-code notification processing via concrete providers
- 📝 **Simple Annotations**: Just `@KafNotifListener` - no implementation needed!
- 🏭 **Production-Ready**: JavaMail, Firebase FCM, Twilio, Slack, Discord, HTTP webhooks
- 🚀 **Auto-Topic Creation**: Automatically creates Kafka topics when first used
- 📧 **Multiple Types**: Email, SMS, Push, Slack, Discord, Webhooks
- 🎯 **Per-Type Config**: Different concurrency, ACK modes, threading per notification type
- 🔧 **Framework Agnostic**: Works with Spring Boot, Quarkus, Vert.x, or standalone Java
- 🧵 **Threading Control**: OS threads, virtual threads, or single-threaded
- 🔄 **Smart ACK Control**: Auto, manual, or manual-immediate acknowledgment
- 🪝 **Optional Hooks**: Add custom logic before/after automatic sending
- ⚡ **High Performance**: Built-in retry mechanisms and dead letter queues

## 🚀 Quick Start (Spring Boot)

### 1. Add Dependency

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.sinamehrabi</groupId>
        <artifactId>KafNotif</artifactId>
        <version>v0.0.7</version>
    </dependency>
</dependencies>
```

[![](https://jitpack.io/v/sinamehrabi/KafNotif.svg)](https://jitpack.io/#sinamehrabi/KafNotif)

### 2. Configure Auto-Processors (ZERO CODE!)

```java
@Component
public class NotificationProcessors {
    
    // 📧 EMAIL: Automatically sent via JavaMail!
    @KafNotifListener(
        value = NotificationType.EMAIL,
        concurrency = 10,
        ackMode = AckMode.MANUAL_IMMEDIATE
    )
    public void emailProcessor() {
        // THAT'S IT! KafNotif automatically sends via JavaMail
        // Optional: Add custom validation/logging here
    }
    
    // 📱 SMS: Automatically sent via Twilio!  
    @KafNotifListener(value = NotificationType.SMS, concurrency = 5)
    public void smsProcessor() {} // Twilio SMS sent automatically!
    
    // 🔔 PUSH: Automatically sent via Firebase!
    @KafNotifListener(value = NotificationType.PUSH, concurrency = 2)
    public void pushProcessor() {} // Firebase FCM sent automatically!
}
```

### 3. Publish Notifications

```java
// Email notification
EmailNotification email = NotificationBuilder.email("user@example.com")
    .subject("Welcome!")
    .body("Welcome to our service")
    .priority(NotificationPriority.HIGH)
    .build();

// SMS notification
SmsNotification sms = NotificationBuilder.sms("+1234567890")
    .message("Your verification code is: 123456")
    .build();

// Push notification
PushNotification push = NotificationBuilder.push("device-token")
    .title("New Message")
    .body("You have a new message")
    .platform(PushNotification.PushPlatform.ANDROID)
    .build();
```

### 4. Publish & Watch Magic Happen!

```java
@RestController
public class NotificationController {
    
    @Autowired
    private NotificationPublisher publisher;
    
    @PostMapping("/send-email")
    public void sendEmail() {
        EmailNotification email = NotificationBuilder.email("user@example.com")
            .subject("Welcome!")
            .body("Welcome to our service")
            .build();
            
        publisher.publish(email); // Auto-creates topic, auto-sends via JavaMail!
    }
}
```

**🎉 Result**: KafNotif automatically sends the email via JavaMail - zero implementation needed!

### 4. Framework-Agnostic Consumer

```java
// Basic consumer
NotificationConsumer consumer = KafNotif.createConsumer("localhost:9092", "my-app");
consumer.start();

// Advanced configuration
ConsumerConfig config = new ConsumerConfig("my-app")
    .bootstrapServers("localhost:9092")
    .threadingMode(ThreadingMode.VIRTUAL_THREADS)
    .concurrency(5)
    .autoCommit(false)
    .hooks(new CustomHooks());

NotificationConsumer consumer = new NotificationConsumer(config);
consumer.start();
```


## Supported Notification Types

| Type | Description | Example Use Cases |
|------|-------------|-------------------|
| 📧 **Email** | HTML/Text emails | User registration, newsletters, reports |
| 📱 **SMS** | Text messages | OTP codes, alerts, reminders |
| 🔔 **Push** | Mobile/Web push notifications | App notifications, breaking news |
| 💬 **Slack** | Slack channel messages | Team notifications, alerts |
| 🎮 **Discord** | Discord webhook messages | Community updates, bot messages |
| 🔗 **Webhook** | HTTP callbacks | Integrations, custom endpoints |

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Producer      │───▶│   Kafka Topics  │───▶│   Consumer      │
│   Service       │    │                 │    │   Service       │
│                 │    │ notifications.* │    │                 │
│ • REST API      │    │ • email         │    │ • Email Provider│
│ • Event Creation│    │ • sms           │    │ • SMS Provider  │
│ • Publishing    │    │ • push          │    │ • Push Provider │
└─────────────────┘    │ • slack         │    │ • Webhook Client│
                       │ • discord       │    └─────────────────┘
                       │ • webhook       │
                       └─────────────────┘
```

## 🎯 Before vs After

### Traditional Way (Manual Implementation)
```java
@KafkaListener(topics = "email-notifications")  
public void processEmail(EmailNotification email) {
    // YOU have to implement all this JavaMail code:
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

### KafNotif Way (Zero Code!)
```java
@KafNotifListener(value = NotificationType.EMAIL, concurrency = 10)
public void emailProcessor() {
    // THAT'S IT! KafNotif automatically sends via JavaMail!
}
```

## 📁 Examples

Check out the [automatic-processing-example](kafnotif-examples/automatic-processing-example/) for a complete Spring Boot demo with:
- 🤖 **Automatic processing** for all notification types  
- 🎛️ **REST API** to test notifications
- ⚙️ **Configuration examples** for real providers
- 🔧 **Console fallbacks** for testing without external services

## Key Features in Detail

### 🚀 Auto-Topic Creation
```java
// Topics are automatically created when first used
NotificationPublisher publisher = KafNotif.createPublisher("localhost:9092");
// Creates: notifications.email, notifications.sms, notifications.push, etc.
```

### 🧵 Threading Modes
```java
ConsumerConfig config = new ConsumerConfig("my-app")
    .threadingMode(ThreadingMode.VIRTUAL_THREADS)  // Java 21+ virtual threads
    .threadingMode(ThreadingMode.PLATFORM_THREADS) // Traditional OS threads
    .threadingMode(ThreadingMode.SINGLE_THREADED)   // Single thread processing
    .concurrency(5); // Number of concurrent consumers
```

### 🪝 Lifecycle Hooks
```java
NotificationHooks hooks = new NotificationHooks() {
    @Override
    public boolean beforeSend(NotificationEvent notification) {
        // Store in database before sending
        database.save(notification);
        return true; // Continue with sending
    }
    
    @Override
    public void afterSend(NotificationEvent notification, boolean success, Throwable error) {
        // Update status in database
        database.updateStatus(notification.getId(), success ? "SENT" : "FAILED");
    }
};

ConsumerConfig config = new ConsumerConfig("my-app").hooks(hooks);
```

### ⚙️ Manual ACK Control
```java
ConsumerConfig config = new ConsumerConfig("my-app")
    .autoCommit(false)    // Manual acknowledgment
    .maxRetries(3)        // Retry failed messages
    .enableDlq(true);     // Dead letter queue for failed messages
```

## ⚙️ Configuration

### Application Properties
```yaml
# KafNotif Core Configuration
kafnotif:
  bootstrap-servers: localhost:9092
  group-id: my-app
  topic-prefix: notifications
  threading-mode: VIRTUAL_THREADS
  concurrency: 5
  ack-mode: MANUAL
  max-retries: 3
  enable-dlq: true
  
  # 🔧 Provider Configuration (Optional - uses console fallback if not set)
  providers:
    # 📧 Email via JavaMail
    email:
      enabled: true
      smtp-host: smtp.gmail.com
      smtp-port: 587
      username: ${EMAIL_USERNAME}
      password: ${EMAIL_APP_PASSWORD}
      from-email: notifications@yourcompany.com
    
    # 🔔 Push via Firebase FCM  
    firebase:
      enabled: true
      service-account-path: ${FIREBASE_SERVICE_ACCOUNT}
    
    # 📱 SMS via Twilio
    twilio:
      enabled: true
      account-sid: ${TWILIO_ACCOUNT_SID}
      auth-token: ${TWILIO_AUTH_TOKEN}
      from-phone: ${TWILIO_FROM_PHONE}
    
    # 💬 Slack via Webhook
    slack:
      enabled: true
      webhook-url: ${SLACK_WEBHOOK_URL}
    
    # 🎮 Discord via Webhook
    discord:
      enabled: true
      webhook-url: ${DISCORD_WEBHOOK_URL}
```

**💡 Smart Fallbacks**: If providers aren't configured, KafNotif uses console logging for testing!

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.