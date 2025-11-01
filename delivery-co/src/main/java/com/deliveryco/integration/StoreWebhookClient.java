package com.deliveryco.integration;

import com.deliveryco.config.properties.StoreWebhookProperties;
import com.deliveryco.entity.DeliveryOrderEntity;
import com.deliveryco.entity.DeliveryStatusEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoreWebhookClient {

    private final WebClient.Builder webClientBuilder;
    private final StoreWebhookProperties props;

    public void sendStatusAsync(DeliveryStatusEventEntity event, DeliveryOrderEntity order) {
        if (props == null || !props.enabled()) {
            return;
        }
        try {
            String url = props.baseUrl() + (props.path() != null ? props.path() : "/api/delivery/status-callback");
            Map<String, Object> payload = toPayload(event, order);
            WebClient client = webClientBuilder.build();
            client.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> {
                        if (props.secretHeader() != null && !props.secretHeader().isBlank()
                                && props.secretValue() != null && !props.secretValue().isBlank()) {
                            h.add(props.secretHeader(), props.secretValue());
                        }
                    })
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(r -> log.info("[Webhook] Sent status {} for order {} -> {}", event.getStatus(), order.getExternalOrderId(), r.getStatusCode()))
                    .doOnError(e -> log.warn("[Webhook] Failed to send status {} for order {}: {}", event.getStatus(), order.getExternalOrderId(), e.toString()))
                    .onErrorResume(e -> Mono.empty())
                    .subscribe();
        } catch (Exception ex) {
            log.warn("[Webhook] Unexpected error preparing request: {}", ex.toString());
        }
    }

    private Map<String, Object> toPayload(DeliveryStatusEventEntity event, DeliveryOrderEntity order) {
        Map<String, Object> m = new HashMap<>();
        m.put("eventId", event.getId());
        m.put("externalOrderId", order.getExternalOrderId());
        m.put("status", event.getStatus().name());
        m.put("occurredAt", OffsetDateTime.from(event.getOccurredAt()));
        m.put("reason", event.getReason());
        return m;
    }
}

