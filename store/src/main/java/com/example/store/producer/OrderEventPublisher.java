package com.example.store.producer;

import com.example.store.event.OrderCancelled;
import com.example.store.event.OrderConfirmed;
import com.example.store.event.OrderPlaced;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    @Value("${app.kafka.topics.orders}")
    private String ORDER_TOPIC;

    private final KafkaTemplate<String, Object> kafka;

    public void publishOrderPlaced(OrderPlaced event) {
        kafka.send(ORDER_TOPIC, event);
    }

    public void publishOrderConfirmed(OrderConfirmed event) {
        kafka.send(ORDER_TOPIC, event);
    }

    public void publishOrderCancelled(OrderCancelled event) {
        kafka.send(ORDER_TOPIC, event);
    }

}
