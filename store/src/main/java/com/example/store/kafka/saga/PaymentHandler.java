package com.example.store.kafka.saga;

import com.example.store.dto.bank.PaymentResponseDTO;
import com.example.store.dto.bank.RefundDTO;
import com.example.store.exception.BankException;
import com.example.store.kafka.command.ChargePayment;
import com.example.store.kafka.command.RefundPayment;
import com.example.store.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(
    id = "payments-handler",
    topics = "#{kafkaTopicProperties.paymentsCommands()}",
    groupId = "payments-handler"
)
@RequiredArgsConstructor
public class PaymentHandler {

    private final PaymentService paymentService;

    // === Consume ChargePayment command
    // === Outbox PaymentSucceeded or PaymentFailed
    @KafkaHandler
    public void on(@Payload @Valid ChargePayment cmd) {
        log.info("@ ChargePayment: order={} createdAt={}", cmd.orderNumber(), cmd.createdAt());

        try {
            PaymentResponseDTO payment = paymentService.transfer(cmd);
            paymentService.onPaymentSucceed(cmd, payment);

        } catch (BankException ex) {
            log.warn("@ ChargePayment: Payment failed for order={}, status={}, message={}", cmd.orderNumber(), ex.getStatusCode(), ex.getMessage());
            paymentService.onPaymentFailed(cmd);

        } catch (Exception ex) {
            log.error("@ ChargePayment: Failed to charge paymeng for order={}: {}", cmd.orderNumber(), ex.getMessage(), ex);
            throw ex;
        }
    }

    // === Consume RefundPayment command
    // === Outbox PaymentSucceeded or PaymentFailed
    @KafkaHandler
    public void on(@Payload @Valid RefundPayment cmd) {
//        log.info("@ RefundPayment: order={} createdAt={}", cmd.orderNumber(), cmd.createdAt());

//        try {
//            RefundDTO payment = paymentService.refund(cmd);
//            paymentService.onPaymentSucceed(cmd, payment);

//        } catch (BankException ex) {
//            log.warn("@ RefundPayment: Payment failed for order={}", cmd.orderNumber(), ex);
//            paymentService.onPaymentFailed(cmd);

//        } catch (Exception ex) {
//            log.error("@ RefundPayment: Failed to charge paymeng for order={}: {}", cmd.orderNumber(), ex.getMessage(), ex);
//            throw ex;
//        }
    }
}
