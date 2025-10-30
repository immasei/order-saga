package com.example.emailservice.controller;

import com.example.emailservice.model.EmailMessage;
import com.example.emailservice.repository.EmailMessageRepository;
import com.example.emailservice.service.EmailSender;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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

	@GetMapping("/toaddress/{toAddress}")
	public ResponseEntity<List<EmailMessage>> getByToAddress(@PathVariable String toAddress) {
		return ResponseEntity.ok(emailMessageRepository.findByToAddress(toAddress));
	}

    @GetMapping
    public ResponseEntity<List<EmailMessage>> listByQuery(@RequestParam(name = "to", required = false) String to) {
        if (to == null || to.isBlank()) {
            return ResponseEntity.ok(emailMessageRepository.findAllByOrderByCreatedAtAsc());
        }
        return ResponseEntity.ok(emailMessageRepository.findByToAddressOrderByCreatedAtAsc(to));
    }

    // Simple SSE stream that emits only unseen messages
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(name = "to", required = false) String to) {
        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> {
            try {
                List<EmailMessage> snapshot = to == null
                        ? emailMessageRepository.findAllByOrderByCreatedAtAsc()
                        : emailMessageRepository.findByToAddressOrderByCreatedAtAsc(to);
                java.util.Set<java.util.UUID> sentIds = new java.util.HashSet<>();
                for (EmailMessage m : snapshot) sentIds.add(m.getId());
                emitter.send(SseEmitter.event().name("init").data(java.util.Collections.emptyList()));
                while (true) {
                    Thread.sleep(2000);
                    List<EmailMessage> now = to == null
                            ? emailMessageRepository.findAllByOrderByCreatedAtAsc()
                            : emailMessageRepository.findByToAddressOrderByCreatedAtAsc(to);
                    for (EmailMessage m : now) {
                        if (!sentIds.contains(m.getId())) {
                            emitter.send(SseEmitter.event().name("message").data(m));
                            sentIds.add(m.getId());
                        }
                    }
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }).start();
        return emitter;
    }

	@PostMapping("/send")
	public ResponseEntity<EmailMessage> sendEmail(@RequestBody EmailMessage request) {
		// Need to specify orderId specifically with the 36 char UUID within strings.
		EmailMessage toPersist = EmailMessage.builder()
				.orderId(request.getOrderId())
				.toAddress(request.getToAddress())
				.externalOrderId(request.getExternalOrderId())
				.subject(request.getSubject())
				.body(request.getBody())
				.status("QUEUED")
				.messageType(request.getMessageType())
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

