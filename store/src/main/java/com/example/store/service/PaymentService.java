package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.payment.ErrorResponse;
import com.example.store.dto.payment.PaymentResponseDTO;
import com.example.store.dto.payment.RefundDTO;
import com.example.store.dto.payment.TransferDTO;
import com.example.store.enums.AggregateType;
import com.example.store.enums.PaymentStatus;
import com.example.store.exception.BankException;
import com.example.store.kafka.command.ChargePayment;
import com.example.store.kafka.command.RefundPayment;
import com.example.store.kafka.event.PaymentFailed;
import com.example.store.kafka.event.PaymentSucceeded;
import com.example.store.model.Order;
import com.example.store.model.Outbox;
import com.example.store.model.Payment;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatusCode;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Qualifier("bankWebClient")
    private final WebClient bankWebClient;

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final KafkaTopicProperties kafkaProps;
    private final PaymentRepository paymentRepository;

    @Value("${store.bank.account.ref}")
    private String bankAccountRef;

    public PaymentResponseDTO transfer(ChargePayment cmd) {
        var req = new TransferDTO();
        req.setAmount(cmd.amount());
        req.setFromAccountRef(cmd.paymentAccountRef());
        req.setToAccountRef(bankAccountRef);
        req.setMemo("Payment for Order: " + cmd.orderNumber());

        return bankWebClient.post()
            .uri("/api/transfer")
            .bodyValue(req)
            .retrieve()
            // handle non-2xx responses
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(ErrorResponse.class)
                    .map(err -> new BankException(
                        err.getStatus(),
                        err.getMessage()
                    ))
            )
            .bodyToMono(PaymentResponseDTO.class)
            .doOnNext(b -> log.info("@ ChargePayment: [Transfer] Bank response: {}", b))
            .timeout(Duration.ofSeconds(5))
            .block();
    }

    public RefundDTO refund(RefundPayment cmd) {
//        Order o = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());

        var req = new RefundDTO();
//        req.setIdempotencyKey(o.getIdempotencyKey());
//        req.setOriginalTransactionRef(o.get);
//        req.setMemo("Refund for Order: " + o.orderNumber());

        return bankWebClient.post()
                .uri("/api/refund")
                .bodyValue(req)
                .retrieve()
                // handle non-2xx responses
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(ErrorResponse.class)
                                .map(err -> new BankException(
                                        err.getStatus(),
                                        err.getMessage()
                                ))
                )
                .bodyToMono(RefundDTO.class)
                .doOnNext(b -> log.info("@ ChargePayment: [Refund] Bank response: {}", b))
                .timeout(Duration.ofSeconds(5))
                .block();
    }

    @Transactional
    public void markPaymentSucceed(ChargePayment cmd, PaymentResponseDTO payment) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(payment.getAmount());
        p.setStatus(PaymentStatus.SUCCESS);
        p.setProviderTxnId(payment.getTransactionRef());
        p.setIdempotencyKey(cmd.idempotencyKey());
        paymentRepository.save(p);

        // 1. build command
        PaymentSucceeded evt = PaymentSucceeded.of(cmd, payment);
        // 2. outbox ChargePayment to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void markPaymentFailed(ChargePayment cmd) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(order.getTotal());
        p.setStatus(PaymentStatus.FAILED);
        p.setProviderTxnId(null);
        p.setIdempotencyKey(cmd.idempotencyKey());
        paymentRepository.save(p);

        // 1. build command
        PaymentFailed evt = PaymentFailed.of(cmd);
        // 2. outbox ChargePayment to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    private void emitEvent(String aggregateId, Class<?> type, Object payload) {
        Outbox outbox = new Outbox();
        outbox.setAggregateId(aggregateId);
        outbox.setAggregateType(AggregateType.PAYMENT);
        outbox.setEventType(type.getName());
        outbox.setTopic(kafkaProps.paymentsEvents());
        outboxService.save(outbox, payload);
    }

}
