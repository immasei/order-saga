package com.example.store.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final WebClientProperties props;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Qualifier("bankWebClient")
    public WebClient bankWebClient(WebClient.Builder builder) {   // <-- inject the Boot builder
        return builder
            .baseUrl(props.getBank())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}

//
//    @Bean
//    @Qualifier("deliveryWebClient")
//    public WebClient deliveryWebClient() {
//        return WebClient.builder()
//                .baseUrl(props.getDeliveryCo())
//                .defaultHeader("Accept", "application/json")
//                .build();
//    }
//}
