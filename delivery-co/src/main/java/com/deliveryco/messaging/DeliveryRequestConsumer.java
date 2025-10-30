package com.deliveryco.messaging;

import com.deliveryco.domain.model.DeliveryRequest;
import com.deliveryco.domain.model.DeliveryRequestItem;
import com.deliveryco.domain.service.OrderLifecycleService;
import com.deliveryco.messaging.dto.DeliveryRequestMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryRequestConsumer {

    private final OrderLifecycleService orderLifecycleService;

    @KafkaListener(topics = "${deliveryco.kafka.request-topic}", groupId = "delivery-co")
    public void onDeliveryRequest(DeliveryRequestMessage message) {
        if (message == null) {
            log.warn("Received null delivery request payload; ignoring");
            return;
        }
        log.info("Received delivery request {} via Kafka", message.externalOrderId());
        DeliveryRequest request = new DeliveryRequest(
                message.externalOrderId(),
                message.customerId(),
                null, // removed in API; multiple warehouses supported via pickupLocations
                null,
                message.dropoffAddress(),
                message.contactEmail(),
                message.lossRate(),
                mapItems(message.items())
        );
        orderLifecycleService.registerDeliveryRequest(request);
    }

    private List<DeliveryRequestItem> mapItems(java.util.Map<String, java.util.Map<String, Integer>> itemsByWarehouse) {
        if (itemsByWarehouse == null || itemsByWarehouse.isEmpty()) return java.util.List.of();
        return itemsByWarehouse.values().stream()
                .flatMap(productMap -> productMap.entrySet().stream()
                        .map(e -> new DeliveryRequestItem(e.getKey(), null, e.getValue())))
                .toList();
    }
}
