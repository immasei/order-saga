package com.example.store.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Inbox {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "source", length = 80, nullable = false)
    private String source;  // Source of the message (e.g. 'external_system', 'internal_service') TODO: enum this

    @Column(name = "message_id", length = 120, nullable = false, unique = true)
    private String messageId;  // Unique ID for each message

    @Column(name = "processed_at")
    private java.time.LocalDateTime processedAt;

    // Constructor
    public Inbox(String source, String messageId, java.time.LocalDateTime processedAt) {
        this.source = source;
        this.messageId = messageId;
        this.processedAt = processedAt;
    }

    // Debugging method
    @Override
    public String toString() {
        return "Inbox{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", messageId='" + messageId + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}
