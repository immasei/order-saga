package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.payment.ErrorResponse;
import com.example.store.dto.payment.PaymentResponseDTO;
import com.example.store.dto.payment.RefundDTO;
import com.example.store.dto.payment.TransferDTO;
import com.example.store.enums.AggregateType;
import com.example.store.enums.EventType;
import com.example.store.enums.OrderStatus;
import com.example.store.enums.PaymentStatus;
import com.example.store.exception.BankException;
import com.example.store.exception.ConflictException;
import com.example.store.exception.NonRefundableException;
import com.example.store.kafka.command.ChargePayment;
import com.example.store.kafka.command.RefundPayment;
import com.example.store.kafka.event.PaymentFailed;
import com.example.store.kafka.event.PaymentRefundRejected;
import com.example.store.kafka.event.PaymentRefunded;
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

import java.math.BigDecimal;
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
    private String bankAccountRef; // store bank account

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
            .doOnNext(b -> log.info("@ ChargePayment: [Bank] response: {}", b))
            .timeout(Duration.ofSeconds(5))
            .block();
    }

    public PaymentResponseDTO refund(RefundPayment cmd, String txId) {
        var req = new RefundDTO();
        req.setIdempotencyKey(cmd.idempotencyKey());
        req.setOriginalTransactionRef(txId);
        req.setMemo("Refund for Order: " + cmd.orderNumber());

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
                .bodyToMono(PaymentResponseDTO.class)
                .doOnNext(b -> log.info("@ RefundPayment: [BANK] response: {}", b))
                .timeout(Duration.ofSeconds(5))
                .block();
    }

    public String getRefundableTx(RefundPayment cmd) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment payment = paymentRepository.findByOrderOrThrow(order);

        final PaymentStatus status = payment.getStatus();
        final String txnId = payment.getProviderTxnId();
        final boolean hasTxnId = txnId != null && !txnId.isBlank();

        if (order.isTerminal())
            throw new NonRefundableException("Order already shipped");

        switch (status) {
            case REFUND_SUCCESS: // mark as refunded
                throw new ConflictException("Refund already processed");

            case PAYMENT_SUCCESS:
                if (!hasTxnId) {
                    throw new NonRefundableException("Provider transaction id is missing");
                }
                return txnId;

            case REFUND_REJECTED, PAYMENT_FAILED: // mark as refunded
                throw new NonRefundableException("Cannot refund: order ends.");

            default:
                throw new NonRefundableException("Unsupported payment status for refund: " + status);
        }
    }

    public boolean isZeroAmount(ChargePayment cmd) {
        return cmd.amount() == null || cmd.amount().compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isZeroAmount(RefundPayment cmd) {
        return cmd.amount() == null || cmd.amount().compareTo(BigDecimal.ZERO) == 0;
    }

    @Transactional
    public void markZeroAmountPaymentSucceed(ChargePayment cmd) {
        // if zero dont talk to bank
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(BigDecimal.ZERO);
        p.setStatus(PaymentStatus.PAYMENT_SUCCESS);
        p.setProviderTxnId(null);
        p.setIdempotencyKey(cmd.idempotencyKey());
        paymentRepository.save(p);

        // 1. build command
        PaymentSucceeded evt = PaymentSucceeded.of(cmd);
        // 2. outbox PaymentSucceeded to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void markZeroAmountRefundSucceed(RefundPayment cmd) {
        // if zero dont talk to bank
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment payment = paymentRepository.findByOrderOrThrow(order);
        payment.setStatus(PaymentStatus.REFUND_SUCCESS);
        paymentRepository.save(payment);

        // 1. build command
        PaymentRefunded evt = PaymentRefunded.refunded(cmd, EventType.PAYMENT_REFUNDED);
        // 2. outbox PaymentSucceeded to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void markPaymentSucceed(ChargePayment cmd, PaymentResponseDTO payment) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(payment.getAmount());
        p.setStatus(PaymentStatus.PAYMENT_SUCCESS);
        p.setProviderTxnId(payment.getTransactionRef());
        p.setIdempotencyKey(cmd.idempotencyKey());
        paymentRepository.save(p);

        // 1. build command
        PaymentSucceeded evt = PaymentSucceeded.of(cmd);
        // 2. outbox PaymentSucceeded to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void markPaymentFailed(ChargePayment cmd) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(order.getTotal());
        p.setStatus(PaymentStatus.PAYMENT_FAILED);
        p.setProviderTxnId(null);
        p.setIdempotencyKey(cmd.idempotencyKey());
        paymentRepository.save(p);

        // 1. build command
        PaymentFailed evt = PaymentFailed.of(cmd);
        // 2. outbox PaymentFailed to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void markRefundSucceed(RefundPayment cmd, PaymentResponseDTO payment) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment p = paymentRepository.findByOrderOrThrow(order);
        p.setStatus(PaymentStatus.REFUND_SUCCESS);
        p.setProviderTxnId(payment.getTransactionRef());
        p.setRefundedTotal(payment.getAmount());
        paymentRepository.save(p);

        // 1. build command
        PaymentRefunded evt = PaymentRefunded.refunded(cmd, EventType.PAYMENT_REFUNDED);
        // 2. outbox PaymentSucceeded to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void markRefundSucceed(RefundPayment cmd) {
        // 1. build command
        PaymentRefunded evt = PaymentRefunded.refunded(cmd, EventType.ALREADY_REFUNDED);
        // 2. outbox PaymentSucceeded to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void markRefundInitiated(RefundPayment cmd) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment p = paymentRepository.findByOrderOrThrow(order);
        p.setStatus(PaymentStatus.PAYMENT_SUCCESS);
        paymentRepository.save(p);

        // 1. build command
        PaymentRefunded evt = PaymentRefunded.refunding(cmd);
        // 2. outbox PaymentSucceeded to db
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void markRefundRejected(RefundPayment cmd) {
        Order order = orderRepository.findByOrderNumberOrThrow(cmd.orderNumber());
        Payment p = paymentRepository.findByOrderOrThrow(order);
        p.setStatus(PaymentStatus.REFUND_REJECTED);
        paymentRepository.save(p);

        // 1. build command
        PaymentRefundRejected evt = PaymentRefundRejected.of(cmd);
        // 2. outbox PaymentFailed to db
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
