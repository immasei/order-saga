package com.example.store.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaTopicProperties props;

    @Bean
    public List<NewTopic> kafkaTopics() {
        return props.getTopics().values().stream()
            .flatMap(base ->
                List.of(
                    new NewTopic(props.commandsOf(base),     props.getPartitions(), props.getReplicas()),
                    new NewTopic(props.eventsOf(base),       props.getPartitions(), props.getReplicas())
                ).stream()
            )
            .collect(Collectors.toList());
    }

}