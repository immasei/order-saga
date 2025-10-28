package com.example.store.producer;

import com.example.store.event.InventoryReleased;
import com.example.store.event.InventoryReserved;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryEventPublisher {

    @Value("${app.kafka.topics.inventory}")
    private String INVENTORY_TOPIC;

    private final KafkaTemplate<String, Object> kafka;

    public void publishReserved(InventoryReserved event) {
        kafka.send(INVENTORY_TOPIC, event);
    }

    public void publishReleased(InventoryReleased event) {
        kafka.send(INVENTORY_TOPIC, event);
    }

}
