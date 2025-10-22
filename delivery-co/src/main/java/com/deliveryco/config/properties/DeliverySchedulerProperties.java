package com.deliveryco.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("deliveryco.scheduler")
public record DeliverySchedulerProperties(
        @DefaultValue("local-worker") String workerId,
        @DefaultValue("PT5S") Duration defaultDelay,
        @DefaultValue("PT2S") Duration jitter,
        @DefaultValue("0.05") double lossRateDefault
) {
}
