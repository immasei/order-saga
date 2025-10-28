package com.example.store.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaTopicConfig {

    private Map<String, String> topics;
    private int partitions;
    private short replicas;

    @Bean
    public List<NewTopic> kafkaTopics() {
        return topics.values().stream()
                .map(t -> new NewTopic(t, partitions, replicas))
                .toList();
    }

}