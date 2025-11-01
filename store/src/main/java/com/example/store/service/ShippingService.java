package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.delivery.DeliveryDTO;
import com.example.store.dto.delivery.DeliveryResponseDTO;
import com.example.store.dto.payment.ErrorResponse;
import com.example.store.enums.AggregateType;
import com.example.store.exception.DeliveryException;
import com.example.store.kafka.command.CreateShipment;
import com.example.store.kafka.event.ShipmentCreated;
import com.example.store.kafka.event.ShipmentFailed;
import com.example.store.model.Outbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    @Qualifier("deliveryWebClient")
    private final WebClient deliveryWebClient;

    private final KafkaTopicProperties kafkaProps;
    private final OutboxService outboxService;

    public DeliveryResponseDTO ship(CreateShipment cmd) {
        var req = new DeliveryDTO();
        req.setExternalOrderId(cmd.orderNumber());
        req.setCustomerId(cmd.customerId());
        req.setPickupLocations(cmd.pickupLocations());
        req.setDropoffAddress(cmd.dropOffAddress());
        req.setContactEmail(cmd.customerEmail());
        req.setLossRate(0.05);
        req.setItems(cmd.productsByWarehouse());

        return deliveryWebClient.post()
            .uri("/api/deliveries")
            .bodyValue(req)
            .retrieve()
            // handle non-2xx responses
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(ErrorResponse.class)
                    .map(err -> new DeliveryException(
                        err.getStatus(),
                        err.getMessage()
                    ))
            )
            .bodyToMono(DeliveryResponseDTO.class)
            .doOnNext(b -> log.info("@ CreateShipment: DeliveryCo response: {}", b))
            .timeout(Duration.ofSeconds(5))
            .block();
    }

    @Transactional
    public void onShipmentCreated(CreateShipment cmd, DeliveryResponseDTO delivery) {
        // 1. build command
        ShipmentCreated evt = ShipmentCreated.of(cmd, delivery);
        // 2. outbox ChargePayment to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void onShipmentFailed(CreateShipment cmd) {
        // 1. build command
        ShipmentFailed evt = ShipmentFailed.of(cmd);
        // 2. outbox ChargePayment to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    private void emitEvent(String aggregateId, Class<?> type, Object payload) {
        Outbox outbox = new Outbox();
        outbox.setAggregateId(aggregateId);
        outbox.setAggregateType(AggregateType.SHIPMENT);
        outbox.setEventType(type.getName());
        outbox.setTopic(kafkaProps.shippingEvents());
        outboxService.save(outbox, payload);
    }
}
