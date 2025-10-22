package com.deliveryco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.deliveryco.domain.model.DeliveryOrderStatus;
import com.deliveryco.entity.DeliveryOrderEntity;
import com.deliveryco.messaging.dto.DeliveryRequestMessage;
import com.deliveryco.messaging.dto.DeliveryRequestMessage.DeliveryRequestItemMessage;
import com.deliveryco.repository.DeliveryOrderRepository;
import com.deliveryco.repository.DeliveryStatusEventRepository;
import com.deliveryco.web.dto.DeliveryRequestDto;
import com.deliveryco.web.dto.DeliveryRequestDto.DeliveryRequestItemDto;
import com.deliveryco.web.dto.DeliveryResponseDto;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DeliveryCoIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("deliveryco")
            .withUsername("deliveryco")
            .withPassword("changeme");

    @Container
    private static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.1"));

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "delivery-co-test");
        registry.add("deliveryco.scheduler.default-delay", () -> "200ms");
        registry.add("deliveryco.scheduler.jitter", () -> "0ms");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private DeliveryOrderRepository orderRepository;

    @Autowired
    private DeliveryStatusEventRepository statusEventRepository;

    @Autowired
    private KafkaTemplate<String, DeliveryRequestMessage> kafkaTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void restEndpointProcessesDeliveryThroughToDelivery() {
        String externalOrderId = "IT-REST-" + UUID.randomUUID();
        DeliveryRequestDto request = new DeliveryRequestDto(
                externalOrderId,
                "C-REST",
                "WH-REST",
                "1 Rest Street",
                "9 Client Road",
                "rest@example.com",
                0.0,
                List.of(new DeliveryRequestItemDto("SKU-R1", "Rest Item", 2))
        );

        DeliveryResponseDto response = restTemplate.postForObject(
                "http://localhost:" + port + "/api/deliveries",
                request,
                DeliveryResponseDto.class
        );

        assertThat(response).isNotNull();
        assertThat(response.externalOrderId()).isEqualTo(externalOrderId);

        UUID deliveryOrderId = response.deliveryOrderId();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            DeliveryOrderEntity order = orderRepository.findById(deliveryOrderId).orElseThrow();
            assertThat(order.getCurrentStatus()).isEqualTo(DeliveryOrderStatus.DELIVERED);
            assertThat(statusEventRepository.findByDeliveryOrderIdOrderByOccurredAt(deliveryOrderId))
                    .extracting(e -> e.getStatus())
                    .contains(DeliveryOrderStatus.RECEIVED,
                            DeliveryOrderStatus.PICKED_UP,
                            DeliveryOrderStatus.IN_TRANSIT,
                            DeliveryOrderStatus.DELIVERED);
        });
    }

    @Test
    void kafkaMessageCreatesDeliveryOrder() throws Exception {
        String externalOrderId = "IT-KAFKA-" + UUID.randomUUID();
        DeliveryRequestMessage message = new DeliveryRequestMessage(
                externalOrderId,
                "C-KAFKA",
                "WH-KAFKA",
                "10 Kafka Depot",
                "20 Kafka Street",
                "kafka@example.com",
                0.0,
                List.of(new DeliveryRequestItemMessage("SKU-K1", "Kafka Item", 3))
        );

        kafkaTemplate.send("delivery.requests", externalOrderId, message).get();

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            DeliveryOrderEntity order = orderRepository.findByExternalOrderId(externalOrderId).orElse(null);
            assertThat(order).isNotNull();
            assertThat(order.getCurrentStatus())
                    .isIn(DeliveryOrderStatus.DELIVERED, DeliveryOrderStatus.LOST);
        });
    }
}
