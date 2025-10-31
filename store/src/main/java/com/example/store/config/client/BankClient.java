//package com.example.store.config.client;
//
//import com.example.store.config.ClientProperties;
//import com.example.store.dto.bank.ApiErrorDTO;
//import com.example.store.dto.bank.PaymentResponseDTO;
//import com.example.store.dto.bank.TransferDTO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class BankClient {
//
//    private final WebClient.Builder webClientBuilder;
//    private final ClientProperties props;
//
//    @Slf4j
//    @Component
//    @RequiredArgsConstructor
//    public class BankClient {
//
//        private final WebClient.Builder webClientBuilder;
//        private final ClientProperties props;
//
//        public PaymentOutcome charge(String idempotencyKey, TransferDTO dto) {
//            try {
//                PaymentResponseDTO response = webClientBuilder.build()
//                        .post()
//                        .uri(props.getBank() + "/api/transactions/transfer")
//                        .header("Idempotency-Key", idempotencyKey)
//                        .bodyValue(dto)
//                        .retrieve()
//                        .onStatus(HttpStatusCode::is4xxClientError, resp ->
//                            resp.bodyToMono(ApiErrorDTO.class).flatMap(err ->
//                                Mono.error(new BankException(err.status(), err.message()))
//                            )
//                        )
//                        .onStatus(HttpStatusCode::is5xxServerError, resp ->
//                                Mono.error(new BankException(500, "Bank internal error"))
//                        )
//                        .bodyToMono(PaymentResponseDTO.class)
//                        .timeout(Duration.ofMillis(3000))
//                        .block();
//
//                // ✅ Successful transaction
//                return new PaymentOutcome.Approved(response);
//
//            } catch (BankException ex) {
//                int code = ex.status();
//                String msg = ex.getMessage();
//
//                if (code == 400 && msg.contains("Transaction failed")) {
//                    // Business logic failure — no retry
//                    return new PaymentOutcome.Declined(
//                        new BankErrorDTO("INSUFFICIENT_FUNDS", msg, null)
//                    );
//                }
//                if (code == 409 || code == 503) {
//                    // Lock timeout or temporary issue — retryable
//                    return new PaymentOutcome.RetryableError(msg);
//                }
//                if (code == 404 || code == 400) {
//                    // Validation or resource error — non-retryable
//                    return new PaymentOutcome.NonRetryableError(msg);
//                }
//                // Fallback for any other status
//                return new PaymentOutcome.NonRetryableError("Unhandled bank error: " + msg);
//            } catch (Exception ex) {
//                // Network/timeout etc.
//                log.warn("Bank call failed: {}", ex.toString());
//                return new PaymentOutcome.RetryableError("Transport/timeout: " + ex.getMessage());
//            }
//        }
//
//        private static class BankException extends RuntimeException {
//            private final int status;
//            public BankException(int status, String msg) { super(msg); this.status = status; }
//            public int status() { return status; }
//        }
//    }
//
//
//    public Mono<String> processRefund(Object paymentRequest) {
//        return webClientBuilder.build()
//                .post()
//                .uri(endpoints.getBank() + "/api/payments")
//                .bodyValue(paymentRequest)
//                .retrieve()
//                .bodyToMono(String.class);
//    }
//}
