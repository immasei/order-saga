package com.deliveryco.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("deliveryco.kafka")
public record DeliveryKafkaProperties(
        @DefaultValue("delivery.requests") String requestTopic,
        @DefaultValue("delivery.status") String statusTopic,
        @DefaultValue("delivery.status.dlq") String deadLetterTopic
) {
}

