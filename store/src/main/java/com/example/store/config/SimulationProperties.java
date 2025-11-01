package com.example.store.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.simulation")
@Getter
@Setter
public class SimulationProperties {
    private double paymentsSuccessRate = 0.8;
    private double shippingSuccessRate = 0.85;
    private double notifySuccessRate = 0.95;
}

