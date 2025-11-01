package com.example.store.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.outbox")
@Getter
@Setter
public class OutboxProperties {
    private int maxAttempts = 8;           // cap retries
    private int backoffInitialSeconds = 2; // first retry
    private int backoffMaxSeconds = 60;    // cap per-attempt delay
}

