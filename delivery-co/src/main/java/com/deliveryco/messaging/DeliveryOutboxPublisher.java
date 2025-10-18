package com.deliveryco.messaging;

import com.deliveryco.config.properties.DeliveryKafkaProperties;
import com.deliveryco.domain.model.OutboxPublishState;
import com.deliveryco.messaging.dto.DeliveryStatusMessage;
import com.deliveryco.repository.DeliveryOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryOutboxPublisher {

    private final DeliveryOutboxRepository outboxRepository;
    private final KafkaTemplate<String, DeliveryStatusMessage> kafkaTemplate;
    private final DeliveryKafkaProperties kafkaProperties;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "PT2S", initialDelayString = "PT5S")
    @Transactional
    public void publishPending() {
        List<com.deliveryco.entity.DeliveryOutboxEntity> pending = outboxRepository
                .findTop20ByPublishStateOrderByCreatedAtAsc(OutboxPublishState.PENDING);
        pending.forEach(this::publishEntry);
        retryFailedOlderThan();
    }

    private void publishEntry(com.deliveryco.entity.DeliveryOutboxEntity entry) {
        entry.setPublishState(OutboxPublishState.IN_FLIGHT);
        entry.setFailureReason(null);
        entry.setUpdatedAt(OffsetDateTime.now());
        try {
            DeliveryStatusMessage message = objectMapper.readValue(entry.getPayload(), DeliveryStatusMessage.class);
            kafkaTemplate.send(kafkaProperties.statusTopic(), message.externalOrderId(), message).get();
            entry.setPublishState(OutboxPublishState.PUBLISHED);
            entry.setPublishedAt(OffsetDateTime.now());
        } catch (JsonProcessingException e) {
            entry.setPublishState(OutboxPublishState.DEAD_LETTERED);
            entry.setFailureReason("Serialization error: " + e.getMessage());
            log.error("Invalid payload for outbox {}", entry.getId(), e);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            entry.setPublishState(OutboxPublishState.FAILED);
            entry.setFailureReason("Interrupted while publishing");
        } catch (Exception ex) {
            log.error("Kafka publish failure for outbox {}", entry.getId(), ex);
            entry.setPublishState(OutboxPublishState.FAILED);
            entry.setFailureReason(ex.getMessage());
        }
    }

    private void retryFailedOlderThan() {
        OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(1);
        List<com.deliveryco.entity.DeliveryOutboxEntity> failedEntries = outboxRepository
                .findByPublishStateAndCreatedAtBefore(OutboxPublishState.FAILED, threshold);
        failedEntries.forEach(entry -> {
            entry.setPublishState(OutboxPublishState.PENDING);
            entry.setUpdatedAt(OffsetDateTime.now());
            entry.setFailureReason("Retrying automatically");
        });
    }
}
