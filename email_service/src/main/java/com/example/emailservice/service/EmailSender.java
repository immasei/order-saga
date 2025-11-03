package com.example.emailservice.service;

import com.example.emailservice.model.EmailMessage;
import com.example.emailservice.repository.EmailMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailSender {

	private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

	private final EmailMessageRepository emailMessageRepository;

	public EmailSender(EmailMessageRepository emailMessageRepository) {
		this.emailMessageRepository = emailMessageRepository;
	}

	public EmailMessage send(EmailMessage message) {
		// For assignment: simulate email send by logging to stdout and marking as SENT
		log.info("[EmailService] Sending email \nto={} \nsubject={} \nbody=\n{}", message.getToAddress(), message.getSubject(), message.getBody());
		message.setStatus("SENT");
		message.setSentAt(LocalDateTime.now());
		return emailMessageRepository.save(message);
	}
}


