package com.example.store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.store.model.UserAccount;
import java.util.UUID;
import reactor.core.publisher.Mono;

@Service
public class UserAccountService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String BASE_URL = "http://localhost:8080/";

    public Mono<UserAccount> getUserById(UUID userId) {
        return webClientBuilder.baseUrl(BASE_URL)
                .build()
                .get()
                .uri("/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserAccount.class);
    }

    public Mono<UserAccount> createUser(UserAccount userAccount) {
        return webClientBuilder.baseUrl(BASE_URL)
                .build()
                .post()
                .uri("/users")
                .bodyValue(userAccount)
                .retrieve()
                .bodyToMono(UserAccount.class);
    }
}
