//package com.example.store.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import com.example.store.model.account.User;
//import java.util.UUID;
//import reactor.core.publisher.Mono;
//
//@Service
//public class UserAccountService {
//
//    @Autowired
//    private WebClient.Builder webClientBuilder;
//
//    private static final String BASE_URL = "http://localhost:8080/";
//
//    public Mono<User> getUserById(UUID userId) {
//        return webClientBuilder.baseUrl(BASE_URL)
//                .build()
//                .get()
//                .uri("/users/{userId}", userId)
//                .retrieve()
//                .bodyToMono(User.class);
//    }
//
//    public Mono<User> createUser(User userAccount) {
//        return webClientBuilder.baseUrl(BASE_URL)
//                .build()
//                .post()
//                .uri("/users")
//                .bodyValue(userAccount)
//                .retrieve()
//                .bodyToMono(User.class);
//    }
//}
