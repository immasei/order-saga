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
            return ResponseEntity.ok(emailMessageRepository.findAll());
        }
        return ResponseEntity.ok(emailMessageRepository.findByToAddress(to));
    }

    // Simple SSE stream placeholder that polls new emails every few seconds
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(name = "to", required = false) String to) {
        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> {
            try {
                List<EmailMessage> last = to == null ? emailMessageRepository.findAll() : emailMessageRepository.findByToAddress(to);
                int lastCount = last.size();
                emitter.send(SseEmitter.event().name("init").data(last));
                while (true) {
                    Thread.sleep(3000);
                    List<EmailMessage> now = to == null ? emailMessageRepository.findAll() : emailMessageRepository.findByToAddress(to);
                    if (now.size() > lastCount) {
                        List<EmailMessage> delta = now.subList(lastCount, now.size());
                        for (EmailMessage m : delta) {
                            emitter.send(SseEmitter.event().name("message").data(m));
                        }
                        lastCount = now.size();
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
