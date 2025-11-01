package com.example.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.endpoints")
public class WebClientProperties {
    private String bank;
    private String deliveryCo;
    private String email;
}
