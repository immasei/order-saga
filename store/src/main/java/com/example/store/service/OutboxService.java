package com.example.store.service;

import com.example.store.model.Outbox;
import com.example.store.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void save(Outbox outbox, Object evt) {
        try {
            String payload = objectMapper.writeValueAsString(evt);

            outbox.setPayload(payload);
            outboxRepository.save(outbox);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event {} for [id={}, eventType={}]",
                outbox.getId(), outbox.getEventType(), e);
            throw new IllegalStateException("Outbox serialization failed", e);

        } catch (Exception e) {
            log.error("Failed to persist outbox message for [id={}, eventType={}], error={}",
                outbox.getId(), outbox.getEventType(), e.getMessage(), e);
            throw new RuntimeException("Failed to persist outbox message", e);
        }
    }

}
