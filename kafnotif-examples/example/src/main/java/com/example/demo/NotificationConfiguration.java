package com.example.demo;

import com.kafnotif.kafka.NotificationPublisher;
import com.kafnotif.spring.AutomaticNotifierSetup;
import com.kafnotif.spring.KafNotifListenerProcessor;
import com.kafnotif.spring.KafNotifProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafNotifProperties.class)
public class NotificationConfiguration {

    @Value("${kafnotif.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafnotif.topic-prefix}")
    private String baseTopic;

    @Bean
    public NotificationPublisher notificationPublisher() {
        // Create publisher with auto-topic creation enabled
        return new NotificationPublisher(bootstrapServers, baseTopic, true, 3, (short) 1);
    }

    @Bean
    public KafNotifListenerProcessor kafNotifListenerProcessor(KafNotifProperties properties) {
        return new KafNotifListenerProcessor(properties);
    }

    @Bean
    public AutomaticNotifierSetup automaticNotifierSetup() {
        return new AutomaticNotifierSetup();
    }
}
