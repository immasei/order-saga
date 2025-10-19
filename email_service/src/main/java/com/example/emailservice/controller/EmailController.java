package com.example.emailservice.controller;

import com.example.emailservice.model.EmailMessage;
import com.example.emailservice.repository.EmailMessageRepository;
import com.example.emailservice.service.EmailSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

	private final EmailMessageRepository emailMessageRepository;
	private final EmailSender emailSender;

	public EmailController(EmailMessageRepository emailMessageRepository, EmailSender emailSender) {
		this.emailMessageRepository = emailMessageRepository;
		this.emailSender = emailSender;
	}

	@GetMapping("/{id}")
	public ResponseEntity<EmailMessage> getById(@PathVariable UUID id) {
		Optional<EmailMessage> maybe = emailMessageRepository.findById(id);
		return maybe.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/order/{orderId}")
	public ResponseEntity<List<EmailMessage>> getByOrder(@PathVariable UUID orderId) {
		return ResponseEntity.ok(emailMessageRepository.findByOrderId(orderId));
	}

	@PostMapping("/send")
	public ResponseEntity<EmailMessage> sendEmail(@RequestBody EmailMessage request) {
		EmailMessage toPersist = EmailMessage.builder()
				.orderId(request.getOrderId())
				.toAddress(request.getToAddress())
				.subject(request.getSubject())
				.body(request.getBody())
				.status("QUEUED")
				.createdAt(LocalDateTime.now())
				.build();
		EmailMessage saved = emailMessageRepository.save(toPersist);
		EmailMessage sent = emailSender.send(saved);
		return ResponseEntity.ok(sent);
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<EmailMessage> updateStatus(@PathVariable UUID id, @RequestParam String status) {
		Optional<EmailMessage> maybe = emailMessageRepository.findById(id);
		if (maybe.isEmpty()) return ResponseEntity.notFound().build();
		EmailMessage msg = maybe.get();
		msg.setStatus(status);
		return ResponseEntity.ok(emailMessageRepository.save(msg));
	}
}


