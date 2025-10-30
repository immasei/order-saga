package com.example.store.kafka.saga;

import com.example.store.model.Outbox;
import com.example.store.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OutboxPublisher {
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafka;

    // Runs every half-second
    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishBatch() {
        Page<Outbox> page = outboxRepository.findPending(PageRequest.of(0, 100));
        for (Outbox row : page.getContent()) {
            if (outboxRepository.tryClaim(row.getId()) == 0) {
                // someone else took it
                continue;
            }

            try {
                String key = row.getAggregateId();   // partition key
                String json = row.getPayload();   // already JSON string
                String topic = row.getTopic();       // kafka topic
                String fqcn = row.getEventType();

                Class<?> clazz = Class.forName(fqcn);
                Object payload = objectMapper.readValue(json, clazz);

                kafka.send(topic, key, payload).get();
                outboxRepository.markSent(row.getId());
                log.info("Outbox SENT topic={}", topic);
            } catch (Exception ex) {
                outboxRepository.markFailed(row.getId());
                log.error("Outbox FAILED id={} err={}", row.getId(), ex.toString(), ex);
            }
        }
    }
}
