package com.example.store.producer;

import com.example.store.event.ShipmentCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingEventPublisher {

    @Value("${app.kafka.topics.shipping}")
    private String SHIPPING_TOPIC;

    private final KafkaTemplate<String, Object> kafka;

    public void publishShipmentCreated(ShipmentCreated event) {
        kafka.send(SHIPPING_TOPIC, event);
    }

}
