package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.notification.EmailDTO;
import com.example.store.dto.notification.EmailResponseDTO;
import com.example.store.dto.payment.ErrorResponse;
import com.example.store.enums.AggregateType;
import com.example.store.enums.EmailStatus;
import com.example.store.enums.EventType;
import com.example.store.exception.EmailException;
import com.example.store.kafka.command.NotifyCustomer;
import com.example.store.kafka.event.EmailFailed;
import com.example.store.kafka.event.EmailSent;
import com.example.store.model.EmailRecord;
import com.example.store.model.Order;
import com.example.store.model.Outbox;
import com.example.store.repository.EmailRecordRepository;
import com.example.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    @Qualifier("emailWebClient")
    private final WebClient emailWebClient;

    private final EmailRecordRepository emailRepository;
    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;
    private final KafkaTopicProperties kafkaProps;
    private final OutboxService outboxService;

    public EmailResponseDTO email(NotifyCustomer cmd) {
        var req = new EmailDTO();
        req.setToAddress(cmd.toAddress());
        req.setExternalOrderId(cmd.orderNumber());
        req.setSubject(cmd.subject());
        req.setBody(cmd.body());

        return emailWebClient.post()
            .uri("/api/emails/send")
            .bodyValue(req)
            .retrieve()
            // handle non-2xx responses
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(ErrorResponse.class)
                    .map(err -> new EmailException(
                        err.getStatus(),
                        err.getMessage()
                    ))
            )
            .bodyToMono(EmailResponseDTO.class)
            .doOnNext(b -> log.info("@ NotifyCustomer: Email-Service response: {}", b))
            .timeout(Duration.ofSeconds(5))
            .block();
    }

    @Transactional
    public void markEmailSent(NotifyCustomer cmd, EmailResponseDTO emailResponseDto) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        EmailRecord email = toEntity(cmd);
        email.setOrder(order);
        email.setStatus(EmailStatus.SENT);
        emailRepository.save(email);

        EmailSent evt = EmailSent.builder()
                .orderNumber(cmd.orderNumber())
                .toAddress(cmd.toAddress())
                .subject(cmd.subject())
                .body(cmd.body())
                .reason(EventType.EMAIL_SENT)
                .createdAt(LocalDateTime.now())
                .build();

        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional(noRollbackFor=WebClientRequestException.class)
    public void markEmailFailed(NotifyCustomer cmd) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        EmailRecord email = toEntity(cmd);
        email.setOrder(order);
        email.setStatus(EmailStatus.FAILED);
        emailRepository.save(email);

        EmailFailed evt = EmailFailed.builder()
            .orderNumber(cmd.orderNumber())
            .toAddress(cmd.toAddress())
            .subject(cmd.subject())
            .body(cmd.body())
            .reason(EventType.EMAIL_FAILED)
            .createdAt(LocalDateTime.now())
            .build();

        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    public EmailRecord toEntity(EmailResponseDTO dto) {
        return modelMapper.map(dto, EmailRecord.class);
    }

    public EmailRecord toEntity(NotifyCustomer cmd) {
        EmailRecord email = new EmailRecord();
        email.setToAddress(cmd.toAddress());
        email.setSubject(cmd.subject());
        email.setBody(cmd.body());

        return email;
    }

    private void emitEvent(String aggregateId, Class<?> type, Object payload) {
        Outbox outbox = new Outbox();
        outbox.setAggregateId(aggregateId);
        outbox.setAggregateType(AggregateType.NOTIFICATION);
        outbox.setEventType(type.getName());
        outbox.setTopic(kafkaProps.notificationsEvents());
        outboxService.save(outbox, payload);
    }
}
