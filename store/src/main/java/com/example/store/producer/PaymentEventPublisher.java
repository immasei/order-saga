package com.example.store.producer;

import com.example.store.event.PaymentProcessed;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {

    @Value("${app.kafka.topics.payments}")
    private String PAYMENT_TOPIC;

    private final KafkaTemplate<String, Object> kafka;

    public void publishProcessed(PaymentProcessed event) {
        kafka.send(PAYMENT_TOPIC, event);
    }
}
