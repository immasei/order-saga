## DeliveryCo Service

DeliveryCo is a Spring Boot microservice responsible for orchestrating the lifecycle of parcel deliveries. It consumes delivery requests from the Store application, schedules asynchronous status transitions, and publishes the resulting state changes to Kafka so that downstream systems (Store, EmailService) can react.

### Architecture Overview

- **Inbound integration**: Kafka consumer (or REST fallback) receives delivery requests and persists them as `DeliveryOrder` aggregates.
- **Persistence**: PostgreSQL accessed through Spring Data JPA; Flyway is used for schema migrations. Entities mirror the ERD (`DeliveryOrder`, `DeliveryItem`, `DeliveryStatusEvent`, `DeliveryJob`, `DeliveryOutbox`, `DeliveryIncident`, `DeliveryWorkerHeartbeat`).
- **Asynchronous scheduling**: `DeliveryJob` records act as durable timers. Worker components claim and execute jobs, updating order status and creating new jobs as needed.
- **Outbox pattern**: Status events destined for Kafka are first written to `DeliveryOutbox`, then a dedicated publisher reliably emits them to the broker with retries and dead-letter handling.
- **Observability**: Spring Boot Actuator, correlation IDs, and incident logging provide diagnostics. Heartbeats allow operators to detect failed workers.

### Quality Attributes

- **Availability & reliability**: Durable jobs/outbox guarantee progress even if API, worker, or publisher nodes fail mid-operation. Optimistic locking and heartbeats coordinate multiple instances without single points of failure.
- **Scalability**: Stateless API tier plus horizontally scalable job/publisher workers. Kafka partitions and job sharding allow linear scaling.
- **Modifiability & maintainability**: Clear separation between domain services, messaging adapters, and persistence via Spring's layered architecture. Domain logic avoids dependence on Kafka/Postgres specifics.
- **Observability**: Structured events with `correlationId`, incidents, and actuator metrics support monitoring and tracing.
- **Security**: Sensitive addresses/emails remain in the persistence layer; configuration supports TLS/Postgres role separation.

### Module Layout

- `com.deliveryco.DeliveryCoApplication` – Spring Boot entry point.
- `com.deliveryco.config` – Infrastructure configuration (Kafka, Postgres, scheduling).
- `com.deliveryco.domain` – Domain services and models (order orchestration, loss simulation).
- `com.deliveryco.entity` – JPA entities defined from the ERD.
- `com.deliveryco.messaging` – Kafka consumers/producers and outbox publisher.
- `com.deliveryco.repository` – Spring Data interfaces.
- `com.deliveryco.web` – REST endpoints (health checks, admin tools).

### Next Steps

1. Configure PostgreSQL, Kafka, and pgAdmin (`docker-compose.yml` is provided) and update `application.yml` with credentials.
2. Implement Kafka topics and consumer/publisher configuration.
3. Expand integration tests using Testcontainers for Postgres and Kafka.

### Building & Running

- Install JDK 17 (e.g., `brew install temurin17`) and point `JAVA_HOME` to it (`export JAVA_HOME=$(/usr/libexec/java_home -v 17)`).
- Install Gradle 8.x (or run `gradle wrapper` if you prefer to generate the Gradle wrapper locally).
- Build: `gradle clean build`
- Run: `gradle bootRun`
