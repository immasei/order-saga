package com.deliveryco.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deliveryco.store-webhook")
public record StoreWebhookProperties(
        boolean enabled,
        String baseUrl,
        String path,
        String secretHeader,
        String secretValue
) {}

