package com.example.store.model;

import com.example.store.model.enums.AggregateType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Outbox {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false)
    private AggregateType aggregateType;  // enum: 'ORDER', 'PAYMENT', 'SHIPMENT'

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", length = 80, nullable = false)
    private String eventType;

    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;  // Storing JSON payload as String (or JSONB??)

    @Column(name = "published", nullable = false, columnDefinition = "boolean default false")
    private boolean published;

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    // Constructor
    public Outbox(AggregateType aggregateType, UUID aggregateId, String eventType, String payload, boolean published, java.time.LocalDateTime createdAt) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.published = published;
        this.createdAt = createdAt;
    }

    // Debugging method
    @Override
    public String toString() {
        return "Outbox{" +
                "id=" + id +
                ", aggregateType='" + aggregateType + '\'' +
                ", aggregateId=" + aggregateId +
                ", eventType='" + eventType + '\'' +
                ", payload='" + payload + '\'' +
                ", published=" + published +
                ", createdAt=" + createdAt +
                '}';
    }
}
