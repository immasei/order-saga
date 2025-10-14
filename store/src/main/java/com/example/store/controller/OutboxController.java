package com.example.store.controller;

import com.example.store.model.Outbox;
import com.example.store.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/outbox")
public class OutboxController {

    @Autowired
    private OutboxRepository outboxRepository;

    // Get outbox entry by ID
    @GetMapping("/{id}")
    public ResponseEntity<Outbox> getOutboxById(@PathVariable UUID id) {
        Optional<Outbox> outbox = outboxRepository.findById(id);
        return outbox.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get outbox entries by aggregate type and ID
    @GetMapping("/aggregate/{aggregateType}/{aggregateId}")
    public ResponseEntity<Outbox> getOutboxByAggregate(@PathVariable String aggregateType, @PathVariable UUID aggregateId) {
        return outboxRepository.findAll()
                .stream()
                .filter(outbox -> outbox.getAggregateType().equals(aggregateType) && outbox.getAggregateId().equals(aggregateId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new outbox entry
    @PostMapping
    public ResponseEntity<Outbox> createOutbox(@RequestBody Outbox outbox) {
        Outbox createdOutbox = outboxRepository.save(outbox);
        return ResponseEntity.ok(createdOutbox);
    }

    // Mark an outbox entry as published
    @PatchMapping("/{id}")
    public ResponseEntity<Outbox> markAsPublished(@PathVariable UUID id) {
        Optional<Outbox> outboxOptional = outboxRepository.findById(id);
        if (outboxOptional.isPresent()) {
            Outbox outbox = outboxOptional.get();
            outbox.setPublished(true);
            outboxRepository.save(outbox);
            return ResponseEntity.ok(outbox);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete outbox entry by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOutbox(@PathVariable UUID id) {
        if (outboxRepository.existsById(id)) {
            outboxRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
