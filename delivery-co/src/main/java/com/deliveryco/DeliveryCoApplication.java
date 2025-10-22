package com.deliveryco;

import com.deliveryco.config.properties.DeliveryKafkaProperties;
import com.deliveryco.config.properties.DeliverySchedulerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({DeliverySchedulerProperties.class, DeliveryKafkaProperties.class})
public class DeliveryCoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryCoApplication.class, args);
    }
}
