package com.kafnotif.spring;

import com.kafnotif.KafNotif;
import com.kafnotif.kafka.NotificationPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for KafNotif Spring Boot integration
 */
@Configuration
@ConditionalOnClass(KafNotif.class)
@EnableConfigurationProperties(KafNotifProperties.class)
@ConditionalOnProperty(name = "kafnotif.enabled", havingValue = "true", matchIfMissing = true)
public class KafNotifAutoConfiguration {

    private final KafNotifProperties properties;

    public KafNotifAutoConfiguration(KafNotifProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationPublisher kafNotifPublisher() {
        return KafNotif.createPublisher(properties.getBootstrapServers(), properties.getTopicPrefix());
    }

    @Bean 
    @ConditionalOnMissingBean
    public KafNotifListenerProcessor kafNotifListenerProcessor() {
        return new KafNotifListenerProcessor(properties);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public AutomaticNotifierSetup automaticNotifierSetup() {
        return new AutomaticNotifierSetup();
    }
}
