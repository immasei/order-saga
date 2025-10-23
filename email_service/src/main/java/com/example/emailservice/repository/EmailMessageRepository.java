package com.example.emailservice.repository;

import com.example.emailservice.model.EmailMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {
	List<EmailMessage> findByOrderId(UUID orderId);

	List<EmailMessage> findByToAddress(String toAddress);
}


