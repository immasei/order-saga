package com.example.store.kafka.saga;

import com.example.store.dto.payment.PaymentResponseDTO;
import com.example.store.exception.BankException;
import com.example.store.exception.ConflictException;
import com.example.store.exception.NonRefundableException;
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
        try {
            if (paymentService.isZeroAmount(cmd)) {
                paymentService.markZeroAmountPaymentSucceed(cmd);
                log.info("@ ChargePayment: [BANK][SKIPPED] amount=0 for order={}", cmd.orderNumber());
            } else {
                PaymentResponseDTO payment = paymentService.transfer(cmd);
                paymentService.markPaymentSucceed(cmd, payment);
                log.info("@ ChargePayment: [BANK][SUCCESS] for order={}, createdAt={}", cmd.orderNumber(), cmd.createdAt());
            }

        } catch (BankException ex) {
            log.warn("@ ChargePayment: [BANK][FAILED] for order={}, status={}, message={}", cmd.orderNumber(), ex.getStatusCode(), ex.getMessage());
            paymentService.markPaymentFailed(cmd);

        } catch (Exception ex) {
            log.error("@ ChargePayment: [BANK][UNEXPECTED] Failed charge payment for order={}: {}", cmd.orderNumber(), ex.getMessage());
            paymentService.markPaymentFailed(cmd);
        }
    }

    // === Consume RefundPayment command
    // === Outbox PaymentRefunded or PaymentRefundRejected
    @KafkaHandler
    public void on(@Payload @Valid RefundPayment cmd) {
        try {
//            throw new BankException(404, "Not Found"); // demo refund failed
            if (paymentService.isZeroAmount(cmd)) {
                paymentService.markZeroAmountRefundSucceed(cmd);
                log.info("@ RefundPayment: [BANK][SKIPPED] amount=0 for order={}", cmd.orderNumber());
            } else {
                String txId = paymentService.getRefundableTx(cmd);
                PaymentResponseDTO payment = paymentService.refund(cmd, txId);
                paymentService.markRefundSucceed(cmd, payment);
                log.info("@ RefundPayment: [BANK][SUCCESS] for order={}, createdAt={}", cmd.orderNumber(), cmd.createdAt());
            }

        } catch (BankException ex) {
            // user are eligible for refund after check
            // however, due to bank error, they are not refunded yet
            log.warn("@ RefundPayment: [BANK][FAILED] for order={}, status={}, message={}", cmd.orderNumber(), ex.getStatusCode(), ex.getMessage());
            paymentService.markRefundInitiated(cmd);

        } catch (ConflictException ex) {
            log.warn("@ RefundPayment: [BANK][SKIPPED] refund already processed for order={}: {}", cmd.orderNumber(), ex.getMessage());
            paymentService.markRefundSucceed(cmd);

        } catch (NonRefundableException ex) {
            log.warn("@ RefundPayment: [BANK][ERR] refund cant be processed for order={}: {}", cmd.orderNumber(), ex.getMessage());
            paymentService.markRefundRejected(cmd);

        } catch (Exception ex) {
            log.error("@ RefundPayment: [BANK][UNEXPECTED] failed to refund payment for order={}: {}", cmd.orderNumber(), ex.getMessage());
            paymentService.markRefundInitiated(cmd);
        }
    }
}
