//package com.example.store.config.client;
//
//import com.example.store.config.ClientProperties;
//import com.example.store.dto.bank.PaymentResponseDTO;
//import com.example.store.dto.bank.TransferDTO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
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
//    public Mono<PaymentResponseDTO> charge(String idempotencyKey, TransferDTO dto) {
//        PaymentResponseDTO webClientBuilder.build()
//            .post()
//            .uri(props.getBank() + "/api/transactions/transfer")
//            .bodyValue(dto)
//            .retrieve()
//            .bodyToMono(PaymentResponseDTO.class)
//            .timeout(Duration.ofMillis(3000))        // per-call timeout
//            .block();
//    }
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
