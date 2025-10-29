package com.example.emailservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_message",
       uniqueConstraints = @UniqueConstraint(name = "uk_email_dedupe",
                                            columnNames = {"to_address", "external_order_id", "message_type"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EmailMessage {

	@Id
	@GeneratedValue
	private UUID id;

	@Column(name = "order_id")
	private UUID orderId;

    @Column(name = "to_address", nullable = false, length = 255)
    private String toAddress;

    // External order id (e.g., ORD-1234) for deduplicating per status
    @Column(name = "external_order_id", length = 64)
    private String externalOrderId;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

	@Column(name = "body", nullable = false, columnDefinition = "TEXT")
	private String body;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // QUEUED, SENT, FAILED

    // Message type mirrors delivery status (RECEIVED, PICKED_UP, IN_TRANSIT, DELIVERED, LOST)
    @Column(name = "message_type", length = 40)
    private String messageType;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;
}
