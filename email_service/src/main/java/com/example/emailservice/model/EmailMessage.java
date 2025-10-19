package com.example.emailservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
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

	@Column(name = "subject", nullable = false, length = 255)
	private String subject;

	@Column(name = "body", nullable = false, columnDefinition = "TEXT")
	private String body;

	@Column(name = "status", nullable = false, length = 32)
	private String status; // QUEUED, SENT, FAILED

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;
}


